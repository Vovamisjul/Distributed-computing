package tasks;

import models.ClientMessage;
import models.Device;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tasks.MessageTypes.ENDED_TASK;
import static tasks.MessageTypes.START;
import static tasks.MessageTypes.STOP;

public abstract class AbstractTaskController {

    protected DeviceRepository repository;

    private BiConsumer<AbstractTaskController, String> finishListener;

    private final String copyId;

    private final String taskId;

    private String authorId;

    private Set<Device> devices = new HashSet<>();

    private final Map<String, DeviceJob> lastDevicesJob = new HashMap<>();

    private final Queue<DeviceJob> notConfirmedJobs = new LinkedList<>();

    private final Queue<Device> lostDevices = new SynchronousQueue<>();

    private volatile boolean end = false;

    public AbstractTaskController(String taskId) {
        this(taskId, UUID.randomUUID().toString());
    }

    public AbstractTaskController(String taskId, String copyId) {
        this.taskId = taskId;
        this.copyId = copyId;
    }

    public void startProcessing(List<Device> freeDevices) {
        devices = new HashSet<>(freeDevices);
        synchronized (this) {
            for (Device device : devices) {
                device.addMessage(getNextClientMessage(device.getId()));
            }
        }
    }

    public synchronized void processClientMessage(String deviceId, ClientMessage message) {
        Device device = repository.getDevice(deviceId);
        if (device.isDisconnected()) {
            onDeviceReconnect(deviceId);
            device.setDisconnected(false);
        }

        switch (message.getType()) {
            case START:
                if (!devices.contains(device)) {
                    Device newDevice = repository.getDevice(deviceId);
                    devices.add(newDevice);
                    newDevice.addMessage(getNextClientMessage(deviceId));
                } else if (!device.isDisconnected()) { // client exited from program, but device was not marked as disconnected
                    ClientMessage answerMsg = new ClientMessage(START, getCopyId());
                    DeviceJob job = lastDevicesJob.get(deviceId);
                    job.fillMessage(answerMsg);
                    device.addMessage(answerMsg);
                }
                break;
            case ENDED_TASK:
                JobResult result = getResultFromMessage(message);
                DeviceJob job = lastDevicesJob.get(deviceId);
                job.addResult(deviceId, result);
                if (job.matchesResult(result)) {
                    if (job.resultCount() > 1) {
                        saveResult(result);
                        for (String jobDevice : job.getDevices()) {
                            repository.getDevice(jobDevice).incRating();
                        }
                    }
                } else { // doesn't match
                    List<String> deviceIds = job.getDevices();
                    if (job.resultCount() == 2) { // need to compare rating or ask 3rd device
                        Device device0 = repository.getDevice(deviceIds.get(0));
                        Device device1 = repository.getDevice(deviceIds.get(1));
                        switch (Device.compareRating(device0, device1, 10)) {
                            case 1:
                                saveResult(job.getResult(deviceIds.get(0)));
                                device0.incRating();
                                device1.decRating();
                                break;
                            case -1:
                                saveResult(job.getResult(deviceIds.get(1)));
                                device1.incRating();
                                device0.decRating();
                                break;
                            case 0:
                                notConfirmedJobs.add(job);
                        }
                    } else { // we are the 3rd device or more
                        JobResult bestResult = findBestResult(job);
                        if (bestResult != null) {
                            saveResult(bestResult);

                            for (String d : deviceIds) {
                                if (bestResult.equals(job.getResult(d))) {
                                    repository.getDevice(d).incRating();
                                } else {
                                    repository.getDevice(d).decRating();
                                }
                            }
                        } else { // try one more device
                            notConfirmedJobs.add(job);
                        }
                    }
                    // else we are the 1st device, do nothing and wait for the second one
                }

                synchronized (this) {
                    device.addMessage(getNextClientMessage(deviceId));
                }
                break;
            default:
                processOtherMessage(deviceId, message);
        }

        if (lastDevicesJob.isEmpty() && notConfirmedJobs.isEmpty()) {
            end = true;
            setResult(getResult());
        }
    }

    /**
     * Finds most popular result in job
     * @return result or null if there are many same best but different results
     */
    private JobResult findBestResult(DeviceJob job) {
        Map<JobResult, Integer> answerCount = new HashMap<>();
        for (String deviceId : job.getDevices()) {
            JobResult result = job.getResult(deviceId);
            if (result != null) {
                if (answerCount.containsKey(result)) {
                    answerCount.put(result, answerCount.get(result)+1);
                } else {
                    answerCount.put(result, 0);
                }
            }
        }
        JobResult bestResult = null;
        int maxCount = -1;
        boolean repeated = false;
        for (Map.Entry<JobResult, Integer> e : answerCount.entrySet()) {
            if (e.getValue() > maxCount) {
                bestResult = e.getKey();
                maxCount = e.getValue();
                repeated = false;
            } else if (e.getValue() == maxCount) {
                repeated = true;
            }
        }
        return repeated ? null : bestResult;
    }

    /**
     * @return ClientMessage - one of:
     * <ul>
     *     <li>stop message (if task ended)</li>
     *     <li>some of not confirmed (if there is)</li>
     *     <li>from lost devices (if such exists)</li>
     *     <li>new (if none of above)</li>
     * </ul>
     */
    protected ClientMessage getNextClientMessage(String deviceId) {

        ClientMessage message = new ClientMessage(START, getCopyId());

        DeviceJob notConfirmed = notConfirmedJobs.peek();
        if (notConfirmed != null && !notConfirmed.hasDevice(deviceId)) {
            notConfirmedJobs.poll();
            notConfirmed.addDevice(deviceId);
            lastDevicesJob.put(deviceId, notConfirmed);
            notConfirmed.fillMessage(message);
            return message;
        }

        Device lostDevice = lostDevices.poll();
        if (lostDevice != null) {
            DeviceJob notProcessed = lastDevicesJob.get(lostDevice.getId());
            notProcessed.addDevice(deviceId);
            lastDevicesJob.put(deviceId, notProcessed);
            notProcessed.fillMessage(message);
            return message;
        }

        if (isEnd()) {
            lastDevicesJob.remove(deviceId);
            return new ClientMessage(STOP, getCopyId());
        }

        DeviceJob job = createDeviceJob();
        job.addDevice(deviceId);
        lastDevicesJob.put(deviceId, job);
        notConfirmedJobs.add(job);

        job.fillMessage(message);
        return message;
    }

    public void onDeviceLost(String deviceId) {
        lostDevices.add(repository.getDevice(deviceId));
    }

    /**
     * Searches all other devices job to find, to whoom its(disconnecteds) task was
     * entrusted, then creates new message to process for entrusted.
     */
    protected void onDeviceReconnect(String deviceId) {
        lostDevices.removeIf(device -> device.getId().equals(deviceId));
        DeviceJob task = lastDevicesJob.get(deviceId);
        List<Map.Entry<String, DeviceJob>> entrustedDeviceJobs =
                lastDevicesJob.entrySet().stream().
                        filter(entry -> entry.getValue().sameAs(task)).
                        filter(entry -> !entry.getKey().equals(deviceId)).
                        collect(Collectors.toList());
        for (Map.Entry<String, DeviceJob> entry : entrustedDeviceJobs) {
            String entrustedDeviceId = entry.getKey();
            repository.getDevice(entrustedDeviceId).addMessage(getNextClientMessage(entrustedDeviceId));
        }
    }

    public void setDeviceRepository(DeviceRepository repository) {
        this.repository = repository;
    }

    /**
     * Should be overrided to process message of some new type
     */
    protected void processOtherMessage(String deviceId, ClientMessage message) {

    }

    /**
     * @return if task precessing ended
     */
    protected abstract boolean isEnd();

    /**
     * @return absolutely new DeviceJob
     */
    protected abstract DeviceJob createDeviceJob();

    /**
     * Transforms message to JobResult
     */
    protected abstract JobResult getResultFromMessage(ClientMessage message);

    /**
     * Should save results from this job, as algorithm marked this result as correct
     */
    protected abstract void saveResult(JobResult job);

    public abstract void setParams(String[] params);

    /**
     * @return serialized task parameters
     */
    public abstract String getParamsAsString();

    /**
     * Sets params from serialized string
     */
    public abstract void setParamsFromString(String params);

    /**
     * @return human readable result
     */
    public abstract String getResult();

    public void setFinishListener(BiConsumer<AbstractTaskController, String> finishListener) {
        this.finishListener = finishListener;
    }

    protected void setResult(String result) {
        finishListener.accept(this, result);
    }

    public String getCopyId() {
        return copyId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}

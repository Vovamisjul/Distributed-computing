package com.vovamisjul.dserver.tasks;

import models.ClientMessage;
import models.Device;
import org.jetbrains.annotations.NotNull;
import tasks.AbstractTaskController;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static tasks.MessageTypes.*;

/**
 * Broots all chats from base64 encoding in same sequence
 * Sends tasks like to broot from '!!!!!' to '"!!!!': that means that values like
 * !!!!!, !!!!", !!!!#, ..., !!!!~, !!!"!, !!!"", ... , ~~~~}, ~~~~~ should be brooted
 * <br>
 * LastHashed - not included
 * <br>
 * Characters - ASCII 0x21/33 (!) to 0x7E/126 (~). More - http://www.asciitable.com/
 */
public class SHA512FinderController extends AbstractTaskController {

    private static final int START_CHAR = 33;
    private static final int END_CHAR = 126;
    private static final BigInteger CHARS_BASE = BigInteger.valueOf(END_CHAR - START_CHAR + 1);

    private String requiredHash;
    private int maxStringLength;
    private volatile BigInteger lastHashed = BigInteger.ZERO;
    private final BigInteger defaultValue = new BigInteger(new byte[]{(byte) 1, 0, 0, 0});
    private Set<Device> devices = new HashSet<>();
    private final Map<String, DeviceJob> lastDevicesJob = new HashMap<>(); // value - vair of start and end string to hash
    private final Queue<DeviceJob> notConfirmedJobs = new LinkedList<>();
    private final List<String> answers = new ArrayList<>();
    private final Queue<Device> lostDevices = new SynchronousQueue<>();
    private volatile boolean end = false;

    SHA512FinderController(String taskId) {
        super(taskId);
    }

    SHA512FinderController(String taskId, String copyId) {
        super(taskId, copyId);
    }

    @Override
    public void startProcessing(List<Device> freeDevices) {
        super.startProcessing(freeDevices);
        devices = new HashSet<>(freeDevices);
        synchronized (this) {
            for (Device device : devices) {
                device.addMessage(creaneNextClientMessage(device.getId()));
            }
        }
    }

    @Override
    public void processClientMessage(String deviceId, ClientMessage message) {
        Device device = repository.getDevice(deviceId);
        if (device.isDisconnected()) {
            onDeviceReconnect(deviceId);
            device.setDisconnected(false);
        }

        switch (message.getType()) {
            case START:
                if (!devices.contains(device)) {
                    processNewDevice(deviceId);
                } else if (!lostDevices.contains(device)) { // client exit from program, but device was not marked as disconnected
                    ClientMessage answerMsg = new ClientMessage(START, getCopyId());
                    answerMsg.addData("required", requiredHash);
                    DeviceJob job = lastDevicesJob.get(deviceId);
                    message.addData("start", bigIntToString(job.start));
                    message.addData("end", bigIntToString(job.end));
                    device.addMessage(answerMsg);
                }
                break;
            case ENDED_TASK:
                JobResult result = new JobResult(Boolean.parseBoolean(message.getData("found")));
                if (message.getData("found").equals("true")) {
                    for (int i = 0; i < Integer.parseInt(message.getData("answerCount")); i++) {
                        String answer = message.getData("answer" + i);
                        result.addAnswer(answer);
                    }
                }

                DeviceJob job = lastDevicesJob.get(deviceId);
                job.addResult(deviceId, result);
                if (job.matchesResult(result)) {
                    if (job.resultCount() > 1) {
                        for (String value : result) {
                            answers.add(value);
                        }
                        for (String jobDevice : job.getDevices()) {
                            repository.getDevice(jobDevice).incRating();
                        }
                    }
                } else { // doesn't match
                    List<String> deviceIds = job.getDevices();
                    if (job.resultCount() == 2) { // need to compare rating or ask 3rd device
                        Device device0 = repository.getDevice(deviceIds.get(0));
                        Device device1 = repository.getDevice(deviceIds.get(1));
                        switch (Device.compareRating(device0, device1, 5)) {
                            case 1:
                                for (String value : job.getResult(deviceIds.get(0))) {
                                    answers.add(value);
                                }
                                device1.decRating();
                                break;
                            case -1:
                                for (String value : job.getResult(deviceIds.get(1))) {
                                    answers.add(value);
                                }
                                device0.decRating();
                                break;
                            case 0:
                                notConfirmedJobs.add(job);
                        }
                    } else if (job.resultCount() == 3) { // we are the 3rd device
                        deviceIds.remove(deviceId);
                        Device device0 = repository.getDevice(deviceIds.get(0));
                        Device device1 = repository.getDevice(deviceIds.get(1));
                        if (job.getResult(deviceIds.get(0)).equals(result)) {
                            device0.incRating();
                            device.incRating();
                            device1.decRating();

                            for (String value : result) {
                                answers.add(value);
                            }
                        } else if (job.getResult(deviceIds.get(1)).equals(result)) {
                            device1.incRating();
                            device.incRating();
                            device0.decRating();

                            for (String value : result) {
                                answers.add(value);
                            }
                        } else { // all answers are different, than apply device with max rating
                            Device maxRating = Stream.of(device, device0, device1).max(Comparator.comparingDouble(Device::getRating)).get();

                            for (String value : job.getResult(maxRating.getId())) {
                                answers.add(value);
                            }
                        }
                    }
                    // else we are the 1st device, do nothing and wait for the second one
                }

                synchronized (this) {
                    device.addMessage(creaneNextClientMessage(deviceId));
                }
                break;
        }

    }

    private synchronized void processNewDevice(String deviceId) {
        Device newDevice = repository.getDevice(deviceId);
        devices.add(newDevice);
        newDevice.addMessage(creaneNextClientMessage(deviceId));
    }

    @Override
    public void setParams(String[] params) {
        requiredHash = params[0];
        maxStringLength = parseInt(params[1]);
    }

    @Override
    public String getParamsAsString() {
        return String.join(" ", Arrays.asList(requiredHash, Integer.toString(maxStringLength)));
    }

    @Override
    public void setParamsFromString(String params) {
        setParams(params.split(" "));
    }

    @Override
    public void onDeviceLost(String deviceId) {
        lostDevices.add(repository.getDevice(deviceId));
    }

    /**
     * Searches all other devices job to find, to whoom its(disconnecteds) task was
     * entrusted, then creates new message to process for entrusted.
     */
    private synchronized void onDeviceReconnect(String deviceId) {
        lostDevices.removeIf(device -> device.getId().equals(deviceId));
        DeviceJob task = lastDevicesJob.get(deviceId);
        List<Map.Entry<String, DeviceJob>> entrustedDeviceJobs =
                lastDevicesJob.entrySet().stream().
                        filter(entry -> entry.getValue().sameAs(task)).
                        filter(entry -> !entry.getKey().equals(deviceId)).
                        collect(Collectors.toList());
        for (Map.Entry<String, DeviceJob> entry : entrustedDeviceJobs) {
            String entrustedDeviceId = entry.getKey();
            repository.getDevice(entrustedDeviceId).addMessage(creaneNextClientMessage(entrustedDeviceId));
        }
    }

    /**
     * Attention!!! This method is not synchronized (due to optimization)
     * and it NEED to be synchronized in caller method.
     */
    private ClientMessage creaneNextClientMessage(String deviceId) {
        if (end) {
            return new ClientMessage(STOP, getCopyId());
        }

        ClientMessage message = new ClientMessage(START, getCopyId());
        message.addData("required", requiredHash);

        DeviceJob notConfirmed = notConfirmedJobs.poll();
        if (notConfirmed != null && !notConfirmed.hasDevice(deviceId)) {
            notConfirmed.addDevice(deviceId);
            lastDevicesJob.put(deviceId, notConfirmed);
            message.addData("start", bigIntToString(notConfirmed.start));
            message.addData("end", bigIntToString(notConfirmed.end));
            return message;
        }

        Device lostDevice = lostDevices.poll();
        if (lostDevice != null) {
            DeviceJob notProcessed = lastDevicesJob.get(lostDevice.getId());
            notProcessed.addDevice(deviceId);
            lastDevicesJob.put(deviceId, notProcessed);
            message.addData("start", bigIntToString(notProcessed.start));
            message.addData("end", bigIntToString(notProcessed.end));
            return message;
        }

        String startStr = bigIntToString(lastHashed);
        if (startStr.length() > maxStringLength) {
            end = true;
            return new ClientMessage(STOP, getCopyId());
        }
        message.addData("start", startStr);
        BigInteger ending = lastHashed.add(defaultValue);
        DeviceJob job = new DeviceJob(lastHashed, ending, deviceId);
        lastDevicesJob.put(deviceId, job);
        notConfirmedJobs.add(job);
        message.addData("end", bigIntToString(ending));
        lastHashed = ending;
        return message;
    }

    private void sendStopMessages() {
        for (Device device : devices) {
            device.addMessage(new ClientMessage(STOP, getCopyId()));
        }
    }

    private String bigIntToString(BigInteger value) {
        String result = "";

        BigInteger[] divResult = value.divideAndRemainder(CHARS_BASE);
        while (divResult[0].compareTo(BigInteger.ZERO) > 0) {
            result = bigIntToChar(divResult[1]) + result;
            divResult = divResult[0].divideAndRemainder(CHARS_BASE);
        }
        result = bigIntToChar(divResult[1]) + result;
        return result;
    }

    private char bigIntToChar(BigInteger value) {
        return (char) (value.intValue() + START_CHAR);
    }

    private static class DeviceJob {
        private final Map<String, JobResult> deviceResults = new HashMap<>();
        private final BigInteger start;
        private final BigInteger end;

        public DeviceJob(BigInteger start, BigInteger end) {
            this.start = start;
            this.end = end;
        }

        public DeviceJob(BigInteger start, BigInteger end, String deviceId) {
            this(start, end);
            addDevice(deviceId);
        }

        public boolean sameAs(DeviceJob that) {
            return Objects.equals(start, that.start) && Objects.equals(end, that.end);
        }

        public void addDevice(String deviceId) {
            deviceResults.put(deviceId, null);
        }

        public boolean hasDevice(String deviceId) {
            return deviceResults.containsKey(deviceId);
        }

        public void addResult(String deviceId, JobResult result) {
            deviceResults.put(deviceId, result);
        }

        public JobResult getResult(String deviceId) {
            return deviceResults.get(deviceId);
        }

        /**
         * @return {@code true} if result matches with all device's results and <br/>
         * {@code false} if result doesn't match with some other device's result
         */
        public boolean matchesResult(JobResult result) {
            return deviceResults.values().stream().allMatch(result::equals);
        }

        public int resultCount() {
            return (int) deviceResults.values().stream().
                    filter(Objects::nonNull)
                    .count();
        }

        public List<String> getDevices() {
            return new ArrayList<>(deviceResults.keySet());
        }
    }

    private static class JobResult implements Iterable<String> {
        private final boolean found;
        private final List<String> answers;

        public JobResult(boolean found, List<String> answers) {
            this.found = found;
            this.answers = answers;
        }

        public JobResult(boolean found) {
            this.found = found;
            this.answers = new ArrayList<>();
        }

        public void addAnswer(String answer) {
            answers.add(answer);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JobResult jobResult = (JobResult) o;
            return found == jobResult.found && answers.equals(jobResult.answers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(found, answers);
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return answers.iterator();
        }
    }
}

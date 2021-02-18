package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.models.ClientMessage;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.vovamisjul.dserver.tasks.MessageTypes.ENDED_TASK;
import static com.vovamisjul.dserver.tasks.MessageTypes.RECONNECT;

/**
 * Broots all chats from base64 encoding in same sequence
 * Sends tasks like to broot from 'AAAAA' to 'AAAAB': that means that values like
 * AAAAA, BAAAA, CAAAA,.../AAAA,ABAAA,BBAAA,..., +///A, ////A should be brooted
 * LastHashed - not included
 */
public class SHA512FinderController extends AbstractTaskController {

    private String requiredHash;
    private int maxStringLength;
    private String id = "123";
    private volatile String lastHashed = "A";
    private List<Device> devices;
    private Map<String, String> lastDevicesJob = new HashMap<>(); // value - start string to hash
    private List<String> answers = new ArrayList<>();
    private Deque<Device> lostDevices = new LinkedList<>();
    private volatile boolean end = false;
    private static final String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    @Override
    public void startProcessing() {
        devices = repository.getAllFreeDevices(id);
        synchronized (this) {
            for (Device device: devices) {
                device.addMessage(creaneNextClientMessage(device.getId()));
            }
        }
    }

    @Override
    public void processClientMessage(String deviceId, ClientMessage message) {
        switch (message.getType()) {
            case ENDED_TASK:
                if (message.getData("found").equals("true")) {
                    answers.add(message.getData("answer"));
                }
                synchronized (this) {
                    repository.getDevice(deviceId).addMessage(creaneNextClientMessage(deviceId));
                }
                break;
        }
    }

    @Override
    public void addNewDevice(String deviceId) {
        synchronized (this) {
            Device newDevice = repository.getDevice(deviceId);
            devices.add(newDevice);
            newDevice.addMessage(creaneNextClientMessage(deviceId));
        }
    }

    @Override
    public void onDeviceLost(String deviceId) {
        lostDevices.add(repository.getDevice(deviceId));
    }

    /**
     * Searches all other devices job to find, to whoom its(disconnecteds) task was
     * entrusted, then creates new message to process for entrusted.
     */
    @Override
    public void onDeviceReconnect(String deviceId, ClientMessage message) {
        synchronized (this) {
            lostDevices.removeIf(device -> device.getId().equals(deviceId));
            String completedHash = lastDevicesJob.get(deviceId);
            Optional<Map.Entry<String, String>> entrustedDeviceJob =
                    lastDevicesJob.entrySet().stream().
                            filter(entry -> entry.getValue().equals(completedHash)).
                            findFirst();
            if (!entrustedDeviceJob.isEmpty()) {
                String entrustedDeviceId = entrustedDeviceJob.get().getKey();
                repository.getDevice(entrustedDeviceId).addMessage(creaneNextClientMessage(entrustedDeviceId));
            }
            processClientMessage(deviceId, message);

        }
    }

    @Override
    public void setParams(Object[] params) {
        requiredHash = (String)params[0];
        maxStringLength = (int)params[1];
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private String getNextEnding(String currentEnding) {
        if (currentEnding.length() < 4) {
            return "AAAA";
        }
        if (currentEnding.length() == 4) {
            return "AAAAA";
        }
        StringBuilder newString = new StringBuilder(currentEnding);
        increaseLetterInStringAtPos(newString, 4);

        return newString.toString();
    }

    private void increaseLetterInStringAtPos(StringBuilder str, int pos) {
        if (pos == str.length()) {
            str.append('A');
            return;
        }
        if (str.charAt(pos) == '/') {
            str.setCharAt(pos, 'A');
            increaseLetterInStringAtPos(str, pos+1);
        } else {
            str.setCharAt(pos, base64chars.charAt(base64chars.indexOf(str.charAt(pos)) + 1));
        }
    }

    /**
     * Attention!!! This method is not synchronized (due to optimization)
     * and it NEED to be synchronized in caller method.
     */
    private ClientMessage creaneNextClientMessage(String deviceId) {
        ClientMessage message = new ClientMessage("start", id);
        message.addData("required", requiredHash);
        Device lostDevice = lostDevices.pollFirst();
        if (lostDevice != null) {
            String notProcessedHashed = lastDevicesJob.get(lostDevice.getId());
            message.addData("start", notProcessedHashed);
            lastDevicesJob.put(deviceId, notProcessedHashed);
            String endHashed = getNextEnding(notProcessedHashed);
            message.addData("end", endHashed);
        } else {
            message.addData("start", lastHashed);
            lastDevicesJob.put(deviceId, lastHashed);
            lastHashed = getNextEnding(lastHashed);
            message.addData("end", lastHashed);
        }
        return message;
    }

    @Override
    public String getDescription() {
        return "Simple task for brooting many strings to find," +
                "which ones sha512 hash matches required one.";
    }
}

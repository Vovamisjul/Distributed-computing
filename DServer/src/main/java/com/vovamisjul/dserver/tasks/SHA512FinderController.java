package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.models.ClientMessage;
import org.javatuples.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static com.vovamisjul.dserver.tasks.MessageTypes.ENDED_TASK;
import static com.vovamisjul.dserver.tasks.MessageTypes.START;
import static java.lang.Integer.parseInt;

/**
 * Broots all chats from base64 encoding in same sequence
 * Sends tasks like to broot from 'AAAAA' to 'AAAAB': that means that values like
 * AAAAA, BAAAA, CAAAA,.../AAAA,ABAAA,BBAAA,..., +///A, ////A should be brooted
 * <br>
 * LastHashed - not included
 * <br>
 * Characters - ASCII 0x21/33 (!) to 0x7E/126 (~)
 */
public class SHA512FinderController extends AbstractTaskController {

    private static int START_CHAR = 33;
    private static int END_CHAR = 126;
    private static BigInteger CHARS_BASE = BigInteger.valueOf(END_CHAR - START_CHAR + 1);

    private String requiredHash;
    private int maxStringLength;
    private volatile BigInteger lastHashed = BigInteger.ZERO;
    private BigInteger defaultValue = new BigInteger(new byte[]{(byte) 1, 0, 0, 0});
    private List<Device> devices = new ArrayList<>();
    private Map<String, Pair<BigInteger, BigInteger>> lastDevicesJob = new HashMap<>(); // value - vair of start and end string to hash
    private List<String> answers = new ArrayList<>();
    private Queue<Device> lostDevices = new LinkedList<>();
    private volatile boolean end = false;

    SHA512FinderController(String taskId) {
        super(taskId);
    }

    @Override
    public void startProcessing(List<Device> freeDevices) {
        super.startProcessing(freeDevices);
        devices = new ArrayList<>(freeDevices);
        synchronized (this) {
            for (Device device : devices) {
                device.addMessage(creaneNextClientMessage(device.getId(), device.getPerformanceRate()));
            }
        }
    }

    @Override
    public void processClientMessage(String deviceId, ClientMessage message) {
        Device device = repository.getDevice(deviceId);
        if (device.isDisconnected()) {
            onDeviceReconnect(deviceId, message);
            device.setDisconnected(false);
        }

        switch (message.getType()) {
            case START:
                addNewDevice(deviceId);
                break;
            case ENDED_TASK:
                if (message.getData("found").equals("true")) {
                    answers.add(message.getData("answer"));
                }
                synchronized (this) {
                    device.addMessage(creaneNextClientMessage(deviceId, device.getPerformanceRate()));
                }
                break;
        }
    }

    private synchronized void addNewDevice(String deviceId) {
        Device newDevice = repository.getDevice(deviceId);
        devices.add(newDevice);
        newDevice.addMessage(creaneNextClientMessage(deviceId, newDevice.getPerformanceRate()));
    }

    @Override
    public void setParams(String[] params) {
        requiredHash = params[0];
        maxStringLength = parseInt(params[1]);
    }

    @Override
    public String getParamsAsString() {
        return Arrays.toString(new Object[]{requiredHash, maxStringLength});
    }

    @Override
    public void onDeviceLost(String deviceId) {
        lostDevices.add(repository.getDevice(deviceId));
    }

    /**
     * Searches all other devices job to find, to whoom its(disconnecteds) task was
     * entrusted, then creates new message to process for entrusted.
     */
    private synchronized void onDeviceReconnect(String deviceId, ClientMessage message) {
            lostDevices.removeIf(device -> device.getId().equals(deviceId));
            Pair<BigInteger, BigInteger> completedHash = lastDevicesJob.get(deviceId);
            Optional<Map.Entry<String, Pair<BigInteger, BigInteger>>> entrustedDeviceJob =
                    lastDevicesJob.entrySet().stream().
                            filter(entry -> entry.getValue().equals(completedHash)).
                            findFirst();
            if (entrustedDeviceJob.isPresent()) {
                String entrustedDeviceId = entrustedDeviceJob.get().getKey();
                repository.getDevice(entrustedDeviceId).addMessage(creaneNextClientMessage(entrustedDeviceId, repository.getDevice(entrustedDeviceId).getPerformanceRate()));
            }
    }

    /**
     * Attention!!! This method is not synchronized (due to optimization)
     * and it NEED to be synchronized in caller method.
     */
    private ClientMessage creaneNextClientMessage(String deviceId, float power) {
        ClientMessage message = new ClientMessage("start", getCopyId());
        message.addData("required", requiredHash);
        Device lostDevice = lostDevices.poll();
        if (lostDevice != null) {
            Pair<BigInteger, BigInteger> notProcessedHashed = lastDevicesJob.get(lostDevice.getId());
            lastDevicesJob.put(deviceId, notProcessedHashed);
            message.addData("start", bigIntToString(notProcessedHashed.getValue0()));
            message.addData("end", bigIntToString(notProcessedHashed.getValue1()));
        } else {

            String startStr = bigIntToString(lastHashed);
            if (startStr.length() > maxStringLength) {
                sendStopMessages();
                setResult(answers.toString());
            }
            message.addData("start", startStr);
            BigInteger ending = lastHashed.add(multiplyBigIntByFloat(defaultValue, power));
            lastDevicesJob.put(deviceId, new Pair<>(lastHashed, ending));
            message.addData("end", bigIntToString(ending));
            lastHashed = ending;
        }
        return message;
    }

    private BigInteger multiplyBigIntByFloat(BigInteger value, float factor) {
        long of1000 = Math.round(Math.ceil(factor * 1000));
        return value.multiply(BigInteger.valueOf(of1000)).divide(BigInteger.valueOf(1000L));
    }

    private void sendStopMessages() {
        for (Device device : devices) {
            device.addMessage(new ClientMessage("stop", getCopyId()));
        }
    }

    private String bigIntToString(BigInteger value) {
        String result = "";

        BigInteger[] divResult = value.divideAndRemainder(CHARS_BASE);
        while (divResult[0].compareTo(BigInteger.ZERO) == 1) {
            result = bigIntToChar(divResult[1]) + result;
            divResult = divResult[0].divideAndRemainder(CHARS_BASE);
        }
        result = bigIntToChar(divResult[1]) + result;
        return result;
    }

    private char bigIntToChar(BigInteger value) {
        return (char) (value.intValue() + START_CHAR);
    }
}

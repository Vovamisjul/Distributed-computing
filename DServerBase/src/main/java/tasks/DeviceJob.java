package tasks;

import models.ClientMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents job, dedicated for devices
 */
public abstract class DeviceJob {

    private final Map<String, JobResult> deviceResults = new HashMap<>();

    /**
     * @return do jobs contain same parameters
     */
    public abstract boolean sameAs(DeviceJob that);

    /**
     * Fills message with params from this job
     */
    public abstract void fillMessage(ClientMessage message);

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

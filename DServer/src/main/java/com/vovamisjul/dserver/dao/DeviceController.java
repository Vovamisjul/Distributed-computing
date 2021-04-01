package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.tasks.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.vovamisjul.dserver.models.JobStatus.READY;

@Component
public class DeviceController implements DeviceRepository {
    @Value("${device.maxTimeToDisconnect}")
    private long maxTimeToDisconnect;

    private ConcurrentMap<String, Device> devices = new ConcurrentHashMap<>();

    public List<Device> getAllFreeDevices(String taskId) {
        return devices.values().stream()
                .filter(device -> device.getAvaliableTasks().contains(taskId))
                .filter(device -> device.getJobStatus() == READY)
                .collect(Collectors.toList());
    }

    public void addDevice(Device device) {
        devices.put(device.getId(), device);
    }

    public void readyToWork(String deviceId) {
        devices.computeIfPresent(deviceId, (key, device) -> {
            device.setJobStatus(READY);
            return device;
        });
    }

    @Override
    public Device getDevice(String deviceId) {
        return devices.get(deviceId);
    }

    public List<Device> getDisconnectedDevices() {
        List<Device> disconnected = devices.values().stream()
                .filter(device -> System.currentTimeMillis() - device.getLastTimeActive() > maxTimeToDisconnect)
                .collect(Collectors.toList());
        disconnected.forEach(device -> device.setDisconnected(true));
        return disconnected;
    }

    public void updatePerformanceRate(String deviceId, float rate) {
        devices.computeIfPresent(deviceId, (id, device) -> {
            device.setPerformanceRate(rate);
            return device;
        });
    }
}

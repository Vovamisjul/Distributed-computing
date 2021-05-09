package tasks;

import models.Device;

public interface DeviceRepository {

    public Device getDevice(String deviceId);

}

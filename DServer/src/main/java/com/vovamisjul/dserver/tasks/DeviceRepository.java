package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.models.Device;

import java.util.List;

public interface DeviceRepository {

    public Device getDevice(String deviceId);

}

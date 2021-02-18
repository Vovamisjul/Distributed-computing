package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.models.ClientMessage;
import com.vovamisjul.dserver.models.Device;

public abstract class AbstractTaskController {

    protected DeviceRepository repository;

    public void setDeviceRepository(DeviceRepository repository) {
        this.repository = repository;
    }

    public abstract void startProcessing();

    public abstract void processClientMessage(String deviceId, ClientMessage message);

    public abstract void addNewDevice(String deviceId);

    public abstract void setParams(Object[] params);

    public abstract String getId();

    public abstract void onDeviceLost(String deviceId);

    public abstract void onDeviceReconnect(String deviceId, ClientMessage message);

    public abstract String getDescription();

}

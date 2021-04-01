package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.models.ClientMessage;
import com.vovamisjul.dserver.models.Device;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractTaskController {

    protected boolean busy;

    protected String result;

    protected DeviceRepository repository;

    private BiConsumer<AbstractTaskController, String> finishListener; // copyId, result

    private String copyId;

    private String taskId;

    AbstractTaskController(String taskId) {
        this.taskId = taskId;
        copyId = UUID.randomUUID().toString();
    }

    public void setDeviceRepository(DeviceRepository repository) {
        this.repository = repository;
    }

    public void startProcessing(List<Device> freeDevices) {
        busy = true;
    }

    public abstract void setParams(String[] params);

    public abstract String getParamsAsString();

    public abstract void processClientMessage(String deviceId, ClientMessage message);

    public abstract void onDeviceLost(String deviceId);

    public boolean isBusy() {
        return busy;
    }

    public String getResult() {
        return result;
    }

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
}

package tasks;

import models.ClientMessage;
import models.Device;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class AbstractTaskController {

    protected boolean busy;

    protected String result;

    protected DeviceRepository repository;

    private BiConsumer<AbstractTaskController, String> finishListener;

    private final String copyId;

    private final String taskId;

    private String authorId;

    public AbstractTaskController(String taskId) {
        this(taskId, UUID.randomUUID().toString());
    }

    public AbstractTaskController(String taskId, String copyId) {
        this.taskId = taskId;
        this.copyId = copyId;
    }

    public void setDeviceRepository(DeviceRepository repository) {
        this.repository = repository;
    }

    public void startProcessing(List<Device> freeDevices) {
        busy = true;
    }

    public abstract void setParams(String[] params);

    public abstract String getParamsAsString();

    public abstract void setParamsFromString(String params);

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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}

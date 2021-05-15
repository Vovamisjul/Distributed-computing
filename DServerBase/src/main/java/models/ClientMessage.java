package models;

import java.util.HashMap;
import java.util.Map;

/**
 * Message from and to client
 * To see required fileds for each message type, see {@link tasks.MessageTypes}
 */
public class ClientMessage {
    private String type;
    private String taskCopyId;
    private Map<String, String> data =  new HashMap<>();

    public ClientMessage() {

    }

    public ClientMessage(String type, String taskCopyId) {
        this.type = type;
        this.taskCopyId = taskCopyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTaskCopyId() {
        return taskCopyId;
    }

    public void setTaskCopyId(String taskCopyId) {
        this.taskCopyId = taskCopyId;
    }

    public void addData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void setData(Map<String, String> data) {
        if (data != null) {
            this.data = data;
        }
    }

    public Map<String, String> getData() {
        return data;
    }
}

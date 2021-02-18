package com.vovamisjul.dserver.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Message from and to client
 * To see required fileds for each message type, see {@link com.vovamisjul.dserver.tasks.MessageTypes}
 */
public class ClientMessage {
    private String type;
    private String taskId;
    private Map<String, String> data =  new HashMap<>();

    public ClientMessage(String type, String taskId) {
        this.type = type;
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void addData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }
}

package com.vovamisjul.dserver.tasks;

public interface MessageTypes {
    // Required fields - ended - boolean, answer - if ended
    public static String ENDED_TASK = "ENDED_TASK";
    public static String RECONNECT = "RECONNECT";
    // Required fields - taskId
    public static String START = "START";
}

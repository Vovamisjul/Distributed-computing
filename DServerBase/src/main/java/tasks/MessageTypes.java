package tasks;

public interface MessageTypes {
    // Required fields - ended - boolean, answer - if ended, answerCount - if answered, answer{i} - i answer
    public static String ENDED_TASK = "ENDED_TASK";
    public static String RECONNECT = "RECONNECT";
    // Required fields - taskId
    public static String START = "START";
    public static String STOP = "STOP";
}

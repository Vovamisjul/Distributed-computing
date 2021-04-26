package com.vovamisjul.dserver.tasks;

public class RunningTaskInfo {

    private final String copyId;
    private final TaskInfo taskInfo;
    private final String authorId;

    public RunningTaskInfo(TaskInfo taskInfo, String copyId, String authorId) {
        this.taskInfo = taskInfo;
        this.copyId = copyId;
        this.authorId = authorId;
    }

    public String getCopyId() {
        return copyId;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public String getAuthorId() {
        return authorId;
    }
}

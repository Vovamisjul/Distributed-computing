package com.vovamisjul.dserver.tasks.objects;

import tasks.TaskInfo;

import java.util.Date;

public class CreatedTaskInfo {
    private final TaskInfo taskInfo;
    private final String copyId;
    private final String author;
    private final String params;
    private final Date created;
    private final String comment;

    public CreatedTaskInfo(TaskInfo taskInfo, String copyId, String author, String params, Date created, String comment) {
        this.taskInfo = taskInfo;
        this.copyId = copyId;
        this.author = author;
        this.params = params;
        this.created = created;
        this.comment = comment;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public String getAuthor() {
        return author;
    }

    public String getParams() {
        return params;
    }

    public Date getCreated() {
        return created;
    }

    public String getComment() {
        return comment;
    }

    public String getCopyId() {
        return copyId;
    }
}

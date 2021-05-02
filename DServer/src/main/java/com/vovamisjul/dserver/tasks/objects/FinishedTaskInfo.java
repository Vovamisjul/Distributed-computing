package com.vovamisjul.dserver.tasks.objects;

import com.vovamisjul.dserver.tasks.TaskInfo;

import java.util.Date;

public class FinishedTaskInfo extends CreatedTaskInfo {
    public String result;
    public Date finished;

    public FinishedTaskInfo(TaskInfo taskInfo, String author, String params, Date created, String comment, String result, Date finished) {
        super(taskInfo, author, params, created, comment);
        this.result = result;
        this.finished = finished;
    }

    public FinishedTaskInfo(CreatedTaskInfo createdTaskInfo, String result, Date finished) {
        this(
                createdTaskInfo.getTaskInfo(),
                createdTaskInfo.getAuthor(),
                createdTaskInfo.getParams(),
                createdTaskInfo.getCreated(),
                createdTaskInfo.getComment(),
                result,
                finished
        );
    }
}

package com.vovamisjul.dserver.tasks.objects;

import com.vovamisjul.dserver.tasks.TaskInfo;

import java.util.Date;

public class QueuedTaskInfo extends CreatedTaskInfo {

    public QueuedTaskInfo(TaskInfo taskInfo, String author, String params, Date created, String comment) {
        super(taskInfo, author, params, created, comment);
    }

    public QueuedTaskInfo(CreatedTaskInfo createdTaskInfo) {
        this(
                createdTaskInfo.getTaskInfo(),
                createdTaskInfo.getAuthor(),
                createdTaskInfo.getParams(),
                createdTaskInfo.getCreated(),
                createdTaskInfo.getComment()
        );
    }
}

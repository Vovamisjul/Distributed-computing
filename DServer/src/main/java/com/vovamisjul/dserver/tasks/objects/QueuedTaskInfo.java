package com.vovamisjul.dserver.tasks.objects;

import com.vovamisjul.dserver.tasks.TaskInfo;

import java.util.Date;

public class QueuedTaskInfo extends CreatedTaskInfo {

    public QueuedTaskInfo(TaskInfo taskInfo, String copyId, String author, String params, Date created, String comment) {
        super(taskInfo, copyId, author, params, created, comment);
    }

    public QueuedTaskInfo(CreatedTaskInfo createdTaskInfo) {
        this(
                createdTaskInfo.getTaskInfo(),
                createdTaskInfo.getCopyId(),
                createdTaskInfo.getAuthor(),
                createdTaskInfo.getParams(),
                createdTaskInfo.getCreated(),
                createdTaskInfo.getComment()
        );
    }
}

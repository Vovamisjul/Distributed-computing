package com.vovamisjul.dserver.tasks.objects;

import com.vovamisjul.dserver.tasks.TaskInfo;

import java.util.Date;

public class RunningTaskInfo extends CreatedTaskInfo {

    public RunningTaskInfo(TaskInfo taskInfo, String author, String params, Date created, String comment) {
        super(taskInfo, author, params, created, comment);
    }

    public RunningTaskInfo(CreatedTaskInfo createdTaskInfo) {
        this(
                createdTaskInfo.getTaskInfo(),
                createdTaskInfo.getAuthor(),
                createdTaskInfo.getParams(),
                createdTaskInfo.getCreated(),
                createdTaskInfo.getComment()
        );
    }
}

package com.vovamisjul.dserver.tasks;

import java.util.Arrays;

public abstract class TaskInfo {

    public abstract String getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract String[] getParamsDescription();

    public abstract AbstractTaskController createTaskController();

    public abstract AbstractTaskController createTaskController(String copyId);
}

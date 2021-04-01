package com.vovamisjul.dserver.tasks;

import java.util.Arrays;

public abstract class TaskInfo {

    public abstract String getId();

    public abstract String getDescription();

    public abstract AbstractTaskController createTaskController();
}

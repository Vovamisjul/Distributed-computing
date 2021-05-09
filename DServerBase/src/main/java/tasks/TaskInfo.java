package tasks;

public abstract class TaskInfo {

    public abstract String getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract String[] getParamsDescription();

    public abstract AbstractTaskController createTaskController();

    public abstract AbstractTaskController createTaskController(String copyId);
}

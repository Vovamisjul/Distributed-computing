package com.vovamisjul.dserver.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TaskControllerRepository {

    private static final Logger LOG = LogManager.getLogger(TaskControllerRepository.class);

    private final Map<String, TaskInfo> taskInfos = new HashMap<>();

    private final Map<String, AbstractTaskController> runningControllers = new HashMap<>();

    @PostConstruct
    public void init() {
        TaskInfo sha512info = new SHA512TaskInfo();
        taskInfos.put(sha512info.getId(), sha512info);
    }

    public List<TaskInfo> getTaskInfos() {
        return new ArrayList<>(taskInfos.values());
    }

    public TaskInfo getTaskInfo(String taskId) {
        return taskInfos.get(taskId);
    }

    public AbstractTaskController createTaskController(String taskId) {
        return taskInfos.get(taskId).createTaskController();
    }

    public AbstractTaskController createTaskController(String taskId, String copyId) {
        return taskInfos.get(taskId).createTaskController(copyId);
    }

    public void addRunningController(AbstractTaskController controller) {
        runningControllers.put(controller.getCopyId(), controller);
    }

    public AbstractTaskController getController(String copyId) {
        return runningControllers.get(copyId);
    }

    public List<AbstractTaskController> getUserControllers(String userId) {
        return runningControllers.values().stream()
                .filter(controller -> controller.getAuthorId().equals(userId))
                .collect(Collectors.toList());
    }

    public boolean hasControllerByTaskId(String taskId) {
        return runningControllers.values().stream().anyMatch(controller -> controller.getTaskId().equals(taskId));
    }

    public List<RunningTaskInfo> getRunningTasks() {
        return runningControllers.values().stream()
                .map(controller -> new RunningTaskInfo(getTaskInfo(controller.getTaskId()), controller.getCopyId(), controller.getAuthorId()))
                .collect(Collectors.toList());
    }
}

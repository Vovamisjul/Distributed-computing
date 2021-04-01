package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.web.filters.JWTDeviceAuthFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

@Component
public class TaskControllerRepository {

    private static Logger LOG = LogManager.getLogger(TaskControllerRepository.class);

    private Map<String, TaskInfo> taskInfos = new HashMap<>();

    private Map<String, AbstractTaskController> runningControllers = new HashMap<>();

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

    public void addRunningController(AbstractTaskController controller) {
        runningControllers.put(controller.getCopyId(), controller);
    }

    public AbstractTaskController getController(String copyId) {
        return runningControllers.get(copyId);
    }

    public boolean hasControllerByTaskId(String taskId) {
        return runningControllers.values().stream().anyMatch(controller -> controller.getTaskId().equals(taskId));
    }
}

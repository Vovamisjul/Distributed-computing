package com.vovamisjul.dserver.tasks;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskControllerRepository {

    private Map<String, AbstractTaskController> taskControllers = new HashMap<>();

    @PostConstruct
    public void init() {
        AbstractTaskController sha512 = new SHA512FinderController();
        taskControllers.put(sha512.getId(), sha512);
    }

    public Map<String, AbstractTaskController> getAllControllers() {
        return taskControllers;
    }

    public AbstractTaskController getTaskController(String taskId) {
        return taskControllers.get(taskId);
    }
}

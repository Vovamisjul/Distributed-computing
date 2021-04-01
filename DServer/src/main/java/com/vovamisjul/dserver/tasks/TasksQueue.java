package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.dao.TaskResultsDao;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class TasksQueue {

    @Autowired
    private TaskControllerRepository taskControllerRepository;
    @Autowired
    private TaskResultsDao taskResultsDao;
    @Autowired
    private DeviceController deviceController;

    private Map<String, Queue<AbstractTaskController>> taskQueue = new HashMap<>(); // Key - taskId

    public String addNewTask(String taskId, String[] params) {
        AbstractTaskController controller = createAndInitController(taskId, params);
        Pair<String, String> result = taskResultsDao.getResultByParams(taskId, controller.getParamsAsString());

        if (result != null) {
            return result.getValue0();
        }

        if (taskControllerRepository.hasControllerByTaskId(taskId)) {
            if (!taskQueue.containsKey(taskId)) {
                taskQueue.put(taskId, new LinkedList<>());
            }
            taskQueue.get(taskId).add(controller);
        } else {
            startNewTask(controller);
        }
        return controller.getCopyId();
    }

    private void startNewTask(AbstractTaskController controller) {
        taskControllerRepository.addRunningController(controller);
        controller.startProcessing(deviceController.getAllFreeDevices(controller.getTaskId()));
    }

    private void onTaskFinish(AbstractTaskController controller, String result) {
        taskResultsDao.saveResult(controller.getCopyId(), controller.getParamsAsString(), controller.getTaskId(), result);
        AbstractTaskController newController = taskQueue.get(controller.getTaskId()).poll();
        if (newController != null) {
            startNewTask(controller);
        }
    }

    private AbstractTaskController createAndInitController(String taskId, String[] params) {
        AbstractTaskController controller = taskControllerRepository.createTaskController(taskId);
        controller.setDeviceRepository(deviceController);
        controller.setFinishListener(this::onTaskFinish);
        controller.setParams(params);
        return controller;
    }

    public Pair<TaskStatus, String> getTaskInfo(String copyId) {
        for (Queue<AbstractTaskController> queue: taskQueue.values()) {
            for (AbstractTaskController controller: queue) {
                if (controller.getCopyId().equals(copyId)) {
                    return new Pair<>(TaskStatus.QUEUED, null);
                }
            }
        }
        // If not in queue, check in progress
        if (taskControllerRepository.getController(copyId) != null) {
            return new Pair<>(TaskStatus.IN_PROGRESS, null);
        }
        // If not in progress, check results
        String result = taskResultsDao.getResultByCopyId(copyId);
        if (result != null) {
            return new Pair<>(TaskStatus.FINISHED, result);
        }
        // Else it doesn't exist
        return new Pair<>(TaskStatus.NOT_EXIST, null);
    }


}

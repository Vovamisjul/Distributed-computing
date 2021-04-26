package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.tasks.AbstractTaskController;
import com.vovamisjul.dserver.tasks.RunningTaskInfo;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.TaskStatus;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Component
public class TasksQueueDao {

    @Autowired
    private TaskControllerRepository taskControllerRepository;
    @Autowired
    private TaskResultsDao taskResultsDao;
    @Autowired
    private DeviceController deviceController;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    // language=SQL
    private static final String ADD_TASK = "INSERT INTO `task_queue` (copy_id, task_id, user_id) VALUE (?,?,?)";
    // language=SQL
    private static final String GET_TASKS = "SELECT `task_id`, `copy_id` FROM `task_queue` WHERE user_id=?";
    // language=SQL
    private static final String EXISTS_TASK = "SELECT EXISTS(SELECT 1 FROM `task_queue` WHERE copy_id=?)";
    // language=SQL
    private static final String POLL_TASK = "SET @id := (SELECT min(`id`) FROM `task_queue` WHERE `task_id`=?);\n" +
            "SELECT * FROM `task_queue` WHERE `id`=@id;\n" +
            "DELETE FROM `task_queue` WHERE `id`=@id;\n";

    public String addNewTask(String taskId, String[] params, String authorId) {
        AbstractTaskController controller = createAndInitController(taskId, params, authorId);
        Pair<String, String> result = taskResultsDao.getResultByParams(taskId, controller.getParamsAsString());

        if (result != null) {
            return result.getValue0();
        }

        if (taskControllerRepository.hasControllerByTaskId(taskId)) {
            jdbcTemplate.update(ADD_TASK, controller.getCopyId(), authorId);
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
        taskResultsDao.saveResult(controller.getCopyId(), controller.getParamsAsString(), controller.getTaskId(), result, controller.getAuthorId());
        AbstractTaskController newController = jdbcTemplate.query(POLL_TASK,
                rs -> {
                    if (rs.next()) {
                        return createAndInitController(
                                rs.getString("copy_id"),
                                rs.getString("task_id"),
                                rs.getString("params"),
                                rs.getString("user_id")
                        );
                    }
                    return null;
                },
                controller.getTaskId());
        if (newController != null) {
            startNewTask(newController);
        }
    }

    private AbstractTaskController createAndInitController(String copyId, String taskId, String params, String authorId) {
        AbstractTaskController controller = taskControllerRepository.createTaskController(taskId, copyId);
        controller.setDeviceRepository(deviceController);
        controller.setFinishListener(this::onTaskFinish);
        controller.setParamsFromString(params);
        controller.setAuthorId(authorId);
        return controller;
    }

    private AbstractTaskController createAndInitController(String taskId, String[] params, String authorId) {
        AbstractTaskController controller = taskControllerRepository.createTaskController(taskId);
        controller.setDeviceRepository(deviceController);
        controller.setFinishListener(this::onTaskFinish);
        controller.setParams(params);
        controller.setAuthorId(authorId);
        return controller;
    }

    public Pair<TaskStatus, String> getTaskInfo(String copyId) {
        //noinspection ConstantConditions
        if (jdbcTemplate.query(
                EXISTS_TASK,
                ResultSet::next,
                copyId
        )) {
            return new Pair<>(TaskStatus.QUEUED, null);
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

    public List<RunningTaskInfo> getQueuedTasks(String userId) {
        return jdbcTemplate.query(GET_TASKS,
                rs -> {
                    List<RunningTaskInfo> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(new RunningTaskInfo(
                                taskControllerRepository.getTaskInfo(rs.getString("task_id")),
                                rs.getString("copy_id"),
                                userId));
                    }
                    return result;
                },
                userId);
    }

    public List<RunningTaskInfo> getRunningTasks(String userId) {
        return taskControllerRepository.getUserControllers(userId).stream()
                .map(controller -> new RunningTaskInfo(
                        taskControllerRepository.getTaskInfo(controller.getTaskId()),
                        controller.getCopyId(),
                        userId
                ))
                .collect(Collectors.toList());
    }

    public List<RunningTaskInfo> getCompletedTasks(String userId) {
        return taskControllerRepository.getUserControllers(userId).stream()
                .map(controller -> new RunningTaskInfo(
                        taskControllerRepository.getTaskInfo(controller.getTaskId()),
                        controller.getCopyId(),
                        userId
                ))
                .collect(Collectors.toList());
    }


}

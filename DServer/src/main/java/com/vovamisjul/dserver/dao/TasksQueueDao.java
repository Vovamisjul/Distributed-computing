package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.tasks.DeviceController;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.TaskStatus;
import com.vovamisjul.dserver.tasks.objects.QueuedTaskInfo;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tasks.AbstractTaskController;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class TasksQueueDao {

    @Autowired
    private TaskControllerRepository taskControllerRepository;
    @Autowired
    private TaskResultsDao taskResultsDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private DeviceController deviceController;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    // language=SQL
    private static final String ADD_TASK_TO_QUEUE = "INSERT INTO `task_queue` (task_copy_id) VALUE (?)";
    // language=SQL
    private static final String GET_TASKS = "SELECT `task_info`.*, username " +
            "FROM `task_queue` " +
            "         JOIN `task_info` on `task_info`.`copy_id` = `task_queue`.`task_copy_id` " +
            "         JOIN `users` on `users`.`id` = `task_info`.`user_id` " +
            "WHERE user_id = ?";
    // language=SQL
    private static final String EXISTS_TASK = "SELECT EXISTS(SELECT 1 FROM `task_queue` WHERE `task_copy_id`=?)";
    // language=SQL
    private static final String POLL_TASK = "SET @id := (SELECT min(`id`) FROM `task_queue` JOIN `task_info` on `task_info`.`copy_id` = `task_queue`.`task_copy_id` WHERE `task_id`=?);\n" +
            "SELECT * FROM `task_queue` JOIN `task_info` on `task_info`.`copy_id` = `task_queue`.`task_copy_id` WHERE `id`=@id;\n" +
            "DELETE FROM `task_queue` WHERE `id`=@id;\n";

    public String addNewTask(String taskId, String[] params, String authorId, String comment) {
        AbstractTaskController controller = createAndInitController(taskId, params, authorId);
        Pair<String, String> result = taskResultsDao.getResultByParams(taskId, controller.getParamsAsString());

        if (result != null) {
            return result.getValue0();
        }

        taskInfoDao.addTaskInfo(controller.getCopyId(), controller.getTaskId(), authorId, controller.getParamsAsString(), comment);
        if (taskControllerRepository.hasControllerByTaskId(taskId)) {
            jdbcTemplate.update(ADD_TASK_TO_QUEUE, controller.getCopyId());
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
        taskResultsDao.saveResult(controller.getCopyId(), result);
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

    public List<QueuedTaskInfo> getQueuedTasks(String userId) {
        return jdbcTemplate.query(GET_TASKS,
                rs -> {
                    List<QueuedTaskInfo> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(new QueuedTaskInfo(
                                taskControllerRepository.getTaskInfo(rs.getString("task_id")),
                                rs.getString("copy_id"),
                                rs.getString("username"),
                                rs.getString("params"),
                                rs.getTimestamp("created", Calendar.getInstance()),
                                rs.getString("comment"))
                        );
                    }
                    return result;
                },
                userId);
    }

}

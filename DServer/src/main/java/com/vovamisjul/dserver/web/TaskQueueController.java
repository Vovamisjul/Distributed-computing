package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.TaskInfoDao;
import com.vovamisjul.dserver.dao.TaskResultsDao;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.TaskStatus;
import com.vovamisjul.dserver.dao.TasksQueueDao;
import com.vovamisjul.dserver.tasks.objects.FinishedTaskInfo;
import com.vovamisjul.dserver.tasks.objects.QueuedTaskInfo;
import com.vovamisjul.dserver.tasks.objects.RunningTaskInfo;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/queue")
public class TaskQueueController {

    @Autowired
    private TasksQueueDao tasksQueueDao;
    @Autowired
    private TaskResultsDao taskResultsDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private TaskControllerRepository taskControllerRepository;

    @RequestMapping(path = "/add", method = RequestMethod.PUT)
    public ResponseEntity<CopyId> addTask(@Valid @RequestBody(required = true) NewTask newTask, HttpServletRequest request) {
        return new ResponseEntity<>(new CopyId(tasksQueueDao.addNewTask(newTask.taskId, newTask.params, request.getRemoteUser(), newTask.comment)), HttpStatus.OK);
    }

    @Validated
    private static class NewTask {
        @NotNull
        public String taskId;
        @NotNull
        public String[] params;
        public String comment;
    }

    private static class CopyId {
        public String copyId;

        public CopyId(String copyId) {
            this.copyId = copyId;
        }
    }

    @RequestMapping(path = "/result", method = RequestMethod.GET)
    public ResponseEntity<TaskResult> getResult(@RequestParam String copyId) {
        Pair<TaskStatus, String> status = tasksQueueDao.getTaskInfo(copyId);
        return new ResponseEntity<>(new TaskResult(status.getValue0(), status.getValue1()), HttpStatus.OK);
    }

    private static class TaskResult {
        public TaskStatus status;
        public String result;

        public TaskResult(TaskStatus status, String result) {
            this.status = status;
            this.result = result;
        }
    }

    @RequestMapping(path = "/queued", method = RequestMethod.GET)
    public ResponseEntity<List<QueuedTaskInfo>> getQueuedTasks(HttpServletRequest request) {
        return new ResponseEntity<>(tasksQueueDao.getQueuedTasks(request.getRemoteUser()), HttpStatus.OK);
    }

    @RequestMapping(path = "/running", method = RequestMethod.GET)
    public ResponseEntity<List<RunningTaskInfo>> getRunningTasks(HttpServletRequest request) {
        return new ResponseEntity<>(
                taskInfoDao.getTaskInfos(taskControllerRepository.getRunningCopyIds(request.getRemoteUser())).stream()
                .map(RunningTaskInfo::new)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @RequestMapping(path = "/completed", method = RequestMethod.GET)
    public ResponseEntity<List<FinishedTaskInfo>> getCompletedTasks(HttpServletRequest request) {
        return new ResponseEntity<>(taskResultsDao.getUserResults(request.getRemoteUser()), HttpStatus.OK);
    }
}

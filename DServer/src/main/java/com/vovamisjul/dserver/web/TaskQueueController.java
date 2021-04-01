package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.tasks.TaskStatus;
import com.vovamisjul.dserver.tasks.TasksQueue;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController()
@RequestMapping("/tasks")
public class TaskQueueController {

    @Autowired
    TasksQueue tasksQueue;

    @RequestMapping(path = "/add", method = RequestMethod.PUT)
    public ResponseEntity<CopyId> addTask(@Valid @RequestBody(required = true) NewTask newTask) {
        return new ResponseEntity<>(new CopyId(tasksQueue.addNewTask(newTask.taskId, newTask.params)), HttpStatus.OK);
    }

    @Validated
    private static class NewTask {
        @NotNull
        public String taskId;
        @NotNull
        public String[] params;
    }

    private static class CopyId {
        public String copyId;

        public CopyId(String copyId) {
            this.copyId = copyId;
        }
    }

    @RequestMapping(path = "/result", method = RequestMethod.GET)
    public ResponseEntity<TaskResult> getResult(@RequestParam String copyId) {
        Pair<TaskStatus, String> status = tasksQueue.getTaskInfo(copyId);
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
}

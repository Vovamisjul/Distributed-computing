package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class SettingsController {

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private TaskControllerRepository taskControllerRepository;

    @RequestMapping(value = "/power", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE)
    public void setPower(
            @Valid @RequestBody(required = true) ComputingPower computingPower,
            HttpServletRequest request
    ) {
        String id = request.getRemoteUser();
        deviceDao.updatePerformance(id, computingPower.timeToCompute);
    }

    @Validated
    private static class ComputingPower {
        @NotNull
        public Float timeToCompute;
    }


    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public List<TaskInfo> getTasks(
            @Valid @RequestBody(required = true) ComputingPower computingPower,
            HttpServletRequest request
    ) {
        return taskControllerRepository.getAllControllers().values().stream()
                .map(controller -> new TaskInfo(controller.getId(), controller.getDescription()))
                .collect(Collectors.toList());
    }

    private class TaskInfo {
        public String id;
        public String description;

        public TaskInfo(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }


    @RequestMapping(value = "/setPossibleTasks", method = RequestMethod.PUT)
    public void setPossibleTasks(
            @RequestBody List<String> taskIds,
            HttpServletRequest request) {
        deviceDao.updatePossibleTasks(request.getRemoteUser(), taskIds);
    }


}

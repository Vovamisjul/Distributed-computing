package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController()
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private DeviceController deviceController;

    @Autowired
    private TaskControllerRepository taskControllerRepository;

    @RequestMapping(value = "/power", method = RequestMethod.PUT)
    public void setPower(
            @Valid @RequestBody(required = true) ComputingPower computingPower,
            HttpServletRequest request
    ) {
        String id = request.getRemoteUser();
        deviceDao.updatePerformance(id, computingPower.rate);
        deviceController.updatePerformanceRate(id, computingPower.rate);
    }

    @Validated
    private static class ComputingPower {
        @NotNull
        public Float rate;
    }


    @RequestMapping(value = "/tasks", method = RequestMethod.GET)
    public List<TaskInfo> getTasks(
            @Valid @RequestBody(required = true) ComputingPower computingPower,
            HttpServletRequest request
    ) {
        return taskControllerRepository.getTaskInfos();
    }


    @RequestMapping(value = "/setPossibleTasks", method = RequestMethod.PUT)
    public void setPossibleTasks(
            @RequestBody List<String> taskIds,
            HttpServletRequest request) {
        deviceDao.updatePossibleTasks(request.getRemoteUser(), taskIds);
    }


}

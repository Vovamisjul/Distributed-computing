package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.TaskInfo;
import com.vovamisjul.dserver.web.filters.JWTTaskAuthFilter;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController()
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private DeviceDao deviceDao;

    @RequestMapping(value = "/setPossibleTasks", method = RequestMethod.PUT)
    public void setPossibleTasks(
            @RequestBody List<String> taskIds,
            HttpServletRequest request) {
        deviceDao.updatePossibleTasks(request.getRemoteUser(), taskIds);
    }

    @RequestMapping(value = "/addPossibleTask", method = RequestMethod.PUT)
    public void setPossibleTasks(
            @Valid @RequestBody Task task,
            HttpServletRequest request) {
        deviceDao.addPossibleTask(request.getRemoteUser(), task.taskId);
    }

    @Validated
    private static class Task {
        @NotNull
        public String taskId;
    }

}

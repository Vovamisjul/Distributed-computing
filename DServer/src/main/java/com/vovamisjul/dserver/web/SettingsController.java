package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.DeviceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;

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

    @RequestMapping(value = "/rating", method = RequestMethod.GET)
    public Rating getRating(HttpServletRequest request) {
        return new Rating(deviceDao.getRating(request.getRemoteUser()));
    }

    @Validated
    private static class Task {
        @NotNull
        public String taskId;
    }

    private static class Rating {
        public double rating;

        public Rating(double rating) {
            this.rating = rating;
        }
    }

}

package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.TaskInfoDao;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.objects.RunningTaskInfo;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tasks.TaskInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/tasks")
public class TaskManageController {

    private static Logger LOG = LogManager.getLogger(SettingsController.class);

    @Autowired
    private TaskControllerRepository taskControllerRepository;
    @Autowired
    private TaskInfoDao taskInfoDao;


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<TaskInfo> getTasks() {
        return taskControllerRepository.getTaskInfos();
    }

    @RequestMapping(value = "/running", method = RequestMethod.GET)
    public List<RunningTaskInfo> getRunningTasks() {
        return taskInfoDao.getTaskInfos(taskControllerRepository.getRunningCopyIds()).stream()
                .map(RunningTaskInfo::new)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public void downloadTask(@PathVariable String name, HttpServletResponse response) {
        try(InputStream inputStream = new FileInputStream("src/main/resources/dl/" + name)) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (FileNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}

package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.tasks.objects.RunningTaskInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import tasks.AbstractTaskController;
import tasks.TaskInfo;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Component
public class TaskControllerRepository {

    private static final Logger LOG = LogManager.getLogger(TaskControllerRepository.class);

    private final Map<String, TaskInfo> taskInfos = new HashMap<>();

    private final Map<String, AbstractTaskController> runningControllers = new HashMap<>();

    @PostConstruct
    public void init() {
        loadJars();
    }

    private void loadJars() {
        try {
            URL jarDirectory = getClass().getResource("/jar");
            if (jarDirectory != null) {
                File jars = new File(jarDirectory.toURI());
                for (File file : jars.listFiles()) {
                    try {
                        JarFile jarFile = new JarFile(file);
                        Enumeration<JarEntry> e = jarFile.entries();

                        URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
                        URLClassLoader cl = URLClassLoader.newInstance(urls);

                        while (e.hasMoreElements()) {
                            JarEntry je = e.nextElement();
                            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                                continue;
                            }
                            // -6 because of .class
                            String className = je.getName().substring(0, je.getName().length() - 6);
                            className = className.replace('/', '.');
                            Class c = cl.loadClass(className);
                            if (TaskInfo.class.isAssignableFrom(c)) {
                                Constructor<TaskInfo>[] ctors = c.getConstructors();
                                for (Constructor<TaskInfo> ctor : ctors) {
                                    if (ctor.getParameterCount() == 0) {
                                        try {
                                            TaskInfo taskInfo = ctor.newInstance();
                                            taskInfos.put(taskInfo.getId(), taskInfo);
                                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
                                        }
                                    }
                                }
                            }

                        }
                    } catch (IOException | ClassNotFoundException ignored) {
                    }
                }
            }
        } catch (URISyntaxException ignored) {
        }
    }

    public List<TaskInfo> getTaskInfos() {
        return new ArrayList<>(taskInfos.values());
    }

    public TaskInfo getTaskInfo(String taskId) {
        return taskInfos.get(taskId);
    }

    public AbstractTaskController createTaskController(String taskId) {
        return taskInfos.get(taskId).createTaskController();
    }

    public AbstractTaskController createTaskController(String taskId, String copyId) {
        return taskInfos.get(taskId).createTaskController(copyId);
    }

    public void addRunningController(AbstractTaskController controller) {
        runningControllers.put(controller.getCopyId(), controller);
    }

    public AbstractTaskController getController(String copyId) {
        return runningControllers.get(copyId);
    }

    public List<AbstractTaskController> getUserControllers(String userId) {
        return runningControllers.values().stream()
                .filter(controller -> controller.getAuthorId().equals(userId))
                .collect(Collectors.toList());
    }

    public boolean hasControllerByTaskId(String taskId) {
        return runningControllers.values().stream().anyMatch(controller -> controller.getTaskId().equals(taskId));
    }

    public List<String> getRunningCopyIds() {
        return runningControllers.values().stream()
                .map(AbstractTaskController::getCopyId)
                .collect(Collectors.toList());
    }

    public List<String> getRunningCopyIds(String userId) {
        return runningControllers.values().stream()
                .filter(controller -> controller.getAuthorId().equals(userId))
                .map(AbstractTaskController::getCopyId)
                .collect(Collectors.toList());
    }
}

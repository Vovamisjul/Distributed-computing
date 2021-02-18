package com.vovamisjul.dserver.tasks;

import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.web.filters.JWTDeviceAuthFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
class DisconnectDetector implements DisposableBean, Runnable {

    private static Logger LOG = LogManager.getLogger(JWTDeviceAuthFilter.class);

    @Value("${device.maxTimeToWait}")
    private long maxTimeToWait;
    @Autowired
    private DeviceController deviceController;
    @Autowired
    private TaskControllerRepository taskControllerRepository;

    private Thread thread;
    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            for (Device device : deviceController.getDisconnectedDevices()) {
                taskControllerRepository.getTaskController(device.getCurrentTaskId()).onDeviceLost(device.getId());
            }
            try {
                Thread.sleep(maxTimeToWait);
            } catch (InterruptedException e) {
                LOG.error("Stopping disconnectDetector", e);
                running = false;
            }
        }
    }

    @Override
    public void destroy() {
        running = false;
    }

}

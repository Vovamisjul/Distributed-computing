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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
class DisconnectDetector {

    private static Logger LOG = LogManager.getLogger(JWTDeviceAuthFilter.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${device.maxTimeToDisconnect}")
    private long maxTimeToDisconnect;
    @Autowired
    private DeviceController deviceController;
    @Autowired
    private TaskControllerRepository taskControllerRepository;

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Device device : deviceController.getDisconnectedDevices()) {
                taskControllerRepository.getController(device.getCurrentTaskCopyId()).onDeviceLost(device.getId());
            }
        }, maxTimeToDisconnect, maxTimeToDisconnect, TimeUnit.MILLISECONDS);
    }

}

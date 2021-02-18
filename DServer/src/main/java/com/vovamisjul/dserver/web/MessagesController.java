package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.models.ClientMessage;
import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.vovamisjul.dserver.models.JobStatus.READY;
import static com.vovamisjul.dserver.tasks.MessageTypes.START;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * To register your device in cahce - send any message
 * To mark your device as redy to work - send READY type
 */
@RestController
public class MessagesController {

    private static Logger LOG = LogManager.getLogger(MessagesController.class);

    @Autowired
    private TaskControllerRepository taskControllerRepository;

    @Autowired
    private DeviceController deviceController;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private ExecutorService executorService;

    /**
     * Processes message from client
     * @return Messages to client to process (like new task, e.g.)
     */
    @RequestMapping(value = "/messages", method = POST, consumes = APPLICATION_JSON_VALUE)
    public DeferredResult<List<ClientMessage>> messages(ClientMessage clientMessage, HttpServletRequest request) {
        DeferredResult<List<ClientMessage>> result = new DeferredResult<>();
        String deviceId = request.getRemoteUser();
        executorService.submit(() -> {
            Device device = deviceController.getDevice(deviceId);
            if (device == null) {
                device = deviceDao.getDevice(deviceId);
                deviceController.addDevice(device);
            }
            device.setLastTimeActive(System.currentTimeMillis());
            if (clientMessage != null) {
                preprocessClientMessage(device, clientMessage);
                taskControllerRepository.getTaskController(clientMessage.getTaskId()).processClientMessage(deviceId, clientMessage);
            }
            try {
                result.setResult(deviceController.getDevice(deviceId).awaitMessages(10_000L));
            } catch (InterruptedException e) {
                LOG.error("Error while getting messages", e);
            }
        });
        return result;
    }

    private void preprocessClientMessage(Device device, ClientMessage clientMessage) {
        switch (clientMessage.getType()) {
            case START:
                device.setJobStatus(READY);
                device.setCurrentTaskId(clientMessage.getData("taskId"));
                break;

        }
    }
}

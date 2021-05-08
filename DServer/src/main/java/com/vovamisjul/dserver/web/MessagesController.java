package com.vovamisjul.dserver.web;

import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.models.ClientMessage;
import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.tasks.AbstractTaskController;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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

    /**
     * Processes message from client
     * @return Messages to client to process (like new task, e.g.)
     */
    @RequestMapping(value = "/messages", method = POST)
    public DeferredResult<ResponseEntity<List<ClientMessage>>> messages(@RequestBody(required = false) ClientMessage clientMessage, HttpServletRequest request) {
        DeferredResult<ResponseEntity<List<ClientMessage>>> result = new DeferredResult<>();
        String deviceId = request.getRemoteUser();
        CompletableFuture.runAsync(() -> {
            Device device = deviceController.getDevice(deviceId);
            if (device == null) {
                device = deviceDao.getDevice(deviceId);
                deviceController.addDevice(device);
                Objects.requireNonNull(device, "Should be not null after auth");
            }
            device.setLastTimeActive(System.currentTimeMillis());
            if (clientMessage != null) {
                preprocessClientMessage(device, clientMessage);
                AbstractTaskController controller = taskControllerRepository.getController(clientMessage.getTaskCopyId());
                if (controller != null) {
                    controller.processClientMessage(deviceId, clientMessage);
                    deviceDao.updateRating(deviceId, device.getRating());
                }
            }
            try {
                result.setResult(new ResponseEntity<>(deviceController.getDevice(deviceId).awaitMessages(10_000L), HttpStatus.OK)) ;
            } catch (Exception e) {
                LOG.error("Error while getting messages", e);
            }
        });
        return result;
    }

    private void preprocessClientMessage(Device device, ClientMessage clientMessage) {
        switch (clientMessage.getType()) {
            case START:
                device.setJobStatus(READY);
                device.setCurrentTaskCopyId(clientMessage.getTaskCopyId());
                break;

        }
    }

    private MultiValueMap<String, String> createError(String error) {
        return new MultiValueMapAdapter<>(Collections.singletonMap("error", Collections.singletonList(error)));
    }

    private interface Responses {
        static String NULL_PERFORMANCE_RATE = "1";
    }
}

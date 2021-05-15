package com.vovamisjul.task;

import models.ClientMessage;
import models.Device;
import tasks.AbstractTaskController;
import tasks.DeviceJob;
import tasks.JobResult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.vovamisjul.task.BigIntUtils.bigIntToString;
import static java.lang.Integer.parseInt;
import static tasks.MessageTypes.ENDED_TASK;
import static tasks.MessageTypes.START;
import static tasks.MessageTypes.STOP;

/**
 * Broots all chats from base64 encoding in same sequence
 * Sends tasks like to broot from '!!!!!' to '"!!!!': that means that values like
 * !!!!!, !!!!", !!!!#, ..., !!!!~, !!!"!, !!!"", ... , ~~~~}, ~~~~~ should be brooted
 * <br>
 * LastHashed - not included
 * <br>
 * Characters - ASCII 0x21/33 (!) to 0x7E/126 (~). More - http://www.asciitable.com/
 */
public class SHA512FinderController extends AbstractTaskController {

    private String requiredHash;
    private int maxStringLength;
    private volatile BigInteger lastHashed = BigInteger.ZERO;
    private final BigInteger defaultValue = new BigInteger(new byte[]{(byte) 1, 0, 0, 0});

    private final List<String> answers = new ArrayList<>();

    SHA512FinderController(String taskId) {
        super(taskId);
    }

    SHA512FinderController(String taskId, String copyId) {
        super(taskId, copyId);
    }

    @Override
    public boolean isEnd() {
        return bigIntToString(lastHashed).length() > maxStringLength;
    }

    @Override
    public void setParams(String[] params) {
        requiredHash = params[0];
        maxStringLength = parseInt(params[1]);
    }

    @Override
    public String getParamsAsString() {
        return String.join(" ", Arrays.asList(requiredHash, Integer.toString(maxStringLength)));
    }

    @Override
    public DeviceJob createDeviceJob() {
        BigInteger ending = lastHashed.add(defaultValue);
        DeviceJob job = new SHA512Job(requiredHash, lastHashed, ending);
        lastHashed = ending;
        return job;
    }

    @Override
    protected JobResult getResultFromMessage(ClientMessage clientMessage) {
        SHA512JobResult result = new SHA512JobResult(Boolean.parseBoolean(clientMessage.getData("found")));
        if (clientMessage.getData("found").equals("true")) {
            for (int i = 0; i < Integer.parseInt(clientMessage.getData("answerCount")); i++) {
                String answer = clientMessage.getData("answer" + i);
                result.addAnswer(answer);
            }
        }
        return result;
    }

    @Override
    protected void saveResult(JobResult jobResult) {
        for (String value : (SHA512JobResult)jobResult) {
            answers.add(value);
        }
    }

    @Override
    public void setParamsFromString(String params) {
        setParams(params.split(" "));
    }

    @Override
    public String getResult() {
        return String.join(" ", answers);
    }

}

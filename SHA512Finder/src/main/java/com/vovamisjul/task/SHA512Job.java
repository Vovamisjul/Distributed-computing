package com.vovamisjul.task;

import models.ClientMessage;
import tasks.DeviceJob;

import java.math.BigInteger;

import static com.vovamisjul.task.BigIntUtils.bigIntToString;

public class SHA512Job extends DeviceJob {

    private final String required;
    private final BigInteger start;
    private final BigInteger end;

    public SHA512Job(String required, BigInteger start, BigInteger end) {
        this.required = required;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean sameAs(DeviceJob deviceJob) {
        return false;
    }

    @Override
    public void fillMessage(ClientMessage clientMessage) {
        clientMessage.addData("required", required);
        clientMessage.addData("start", bigIntToString(start));
        clientMessage.addData("end", bigIntToString(end));
    }
}

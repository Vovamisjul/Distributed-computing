package com.vovamisjul.dserver.models;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.*;

import static com.vovamisjul.dserver.models.JobStatus.UNACTIVE;

@ParametersAreNonnullByDefault
public class Device {
    private String id;
    @Nullable
    private volatile Float performanceRate;
    private volatile JobStatus jobStatus = UNACTIVE;
    private volatile List<ClientMessage> unsentMessages = new LinkedList<>();
    private Set<String> avaliableTasks = new HashSet<>();
    private volatile String currentTaskId;
    private volatile long lastTimeActive;
    private volatile boolean disconnected;

    public Device() {
    }

    public Device(String id) {
        this.id = id;
    }

    public synchronized void addMessage(ClientMessage message) {
        unsentMessages.add(message);
        this.notify();
    }

    public List<ClientMessage> takeMessages() {
        List<ClientMessage> returnValue = new ArrayList<>();
        synchronized (this) {
            Collections.copy(returnValue, unsentMessages);
            unsentMessages.clear();
        }
        return returnValue;
    }

    public synchronized List<ClientMessage> awaitMessages(long timeout) throws InterruptedException {
        if (unsentMessages.isEmpty()) {
            this.wait(timeout);
        }
        return takeMessages();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public float getPerformanceRate() {
        return performanceRate;
    }

    public void setPerformanceRate(Float performanceRate) {
        this.performanceRate = performanceRate;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Set<String> getAvaliableTasks() {
        return avaliableTasks;
    }

    public void setAvaliableTasks(Set<String> avaliableTasks) {
        this.avaliableTasks = avaliableTasks;
    }

    public long getLastTimeActive() {
        return lastTimeActive;
    }

    public void setLastTimeActive(long lastTimeActive) {
        this.lastTimeActive = lastTimeActive;
    }

    public String getCurrentTaskId() {
        return currentTaskId;
    }

    public void setCurrentTaskId(String currentTaskId) {
        this.currentTaskId = currentTaskId;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

package models;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static models.JobStatus.UNACTIVE;

@ParametersAreNonnullByDefault
public class Device {
    private String id;
    private volatile double rating;
    private volatile JobStatus jobStatus = UNACTIVE;
    private final List<ClientMessage> unsentMessages = new LinkedList<>();
    private Set<String> availableTasks = new HashSet<>();
    private volatile String currentTaskCopyId;
    private volatile long lastTimeActive;
    private volatile boolean disconnected;

    public Device() {
    }

    public Device(String id) {
        this.id = id;
    }

    public synchronized void addMessage(@Nullable ClientMessage message) {
        if (message != null) {
            unsentMessages.add(message);
            this.notify();
        }
    }

    public List<ClientMessage> takeMessages() {
        List<ClientMessage> returnValue;
        synchronized (this) {
            returnValue = new ArrayList<>(unsentMessages);
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

    public static int compareRating(Device d1, Device d2, int gapToEqual) {
        long thisSteps = getRatingSteps(d1.rating), otherSteps = getRatingSteps(d2.rating);
        int diff = (int) (thisSteps - otherSteps);
        if (Math.abs(diff) > gapToEqual) {
            return diff > 0 ? 1 : -1;
        }
        return 0;
    }

    private static long getRatingSteps(double rating) {
        if (rating >= 0.5) {
            return Math.round(- Math.log(1 - rating) / Math.log(2) - 1);
        } else {
            return Math.round((rating) / Math.log(2) + 1);
        }
    }

    public synchronized void incRating() {
        if (rating >= 0.5) {
            rating += (1 - rating) / 2;
        } else {
            rating *= 2;
        }
    }

    public synchronized void decRating() {
        if (rating > 0.5) {
            rating -= 1 - rating;
        } else {
            rating /= 2;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Set<String> getAvailableTasks() {
        return availableTasks;
    }

    public void setAvailableTasks(Set<String> availableTasks) {
        this.availableTasks = availableTasks;
    }

    public long getLastTimeActive() {
        return lastTimeActive;
    }

    public void setLastTimeActive(long lastTimeActive) {
        this.lastTimeActive = lastTimeActive;
    }

    public String getCurrentTaskCopyId() {
        return currentTaskCopyId;
    }

    public void setCurrentTaskCopyId(String currentTaskCopyId) {
        this.currentTaskCopyId = currentTaskCopyId;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return id.equals(device.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

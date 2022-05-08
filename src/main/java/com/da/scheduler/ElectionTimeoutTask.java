package com.da.scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ElectionTimeoutTask {
    private final ScheduledFuture<?> scheduledFuture;
    public static final ElectionTimeoutTask NONE = new ElectionTimeoutTask(new NullScheduledFuture());

    public ElectionTimeoutTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    // 取消选举超时
    public void cancel() {
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        if (this.scheduledFuture.isCancelled()) {
            return "ElectionTimeoutTask(state=cancelled)";
        }
        if (this.scheduledFuture.isDone()) {
            return "ElectionTimeoutTask(state=done)";
        }
        return "ElectionTimeout{delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms}";
    }
}

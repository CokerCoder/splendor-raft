package com.da.scheduler;

import java.util.concurrent.ScheduledFuture;

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
}

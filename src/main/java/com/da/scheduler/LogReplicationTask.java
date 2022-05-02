package com.da.scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LogReplicationTask {

    private final ScheduledFuture<?> scheduledFuture;

    public static final LogReplicationTask NONE = new LogReplicationTask(new NullScheduledFuture());

    public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    // 取消日志复制定时器
    public void cancel() {
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "LogReplicationTask{delay=}" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "}";
    }



}

package com.da.scheduler;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// 默认计时器实现，定义了超时选举和日志复制任务的相关配置
public class DefaultScheduler implements Scheduler {

    private final int minElectionTimeout;
    private final int maxElectionTimeout;
    private final Random electionTimeoutRandom;

    private final int logReplicationDelay;
    private final int logReplicationInterval;

    private final ScheduledExecutorService scheduledExecutorService;


    public DefaultScheduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationDelay, int logReplicationInterval) {
        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 || minElectionTimeout > maxElectionTimeout) {
            throw new IllegalArgumentException("election timeout should not be 0 or min > max");
        }
        if (logReplicationDelay < 0 || logReplicationInterval <= 0) {
            throw new IllegalArgumentException("log replication delay < 0 or log replication interval <= 0");
        }
        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        this.logReplicationDelay = logReplicationDelay;
        this.logReplicationInterval = logReplicationInterval;
        electionTimeoutRandom = new Random();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "scheduler"));
    }

    @Override
    public ElectionTimeoutTask scheduleElectionTimeoutTask(Runnable task) {
        // 在选举超时区间内选择一个超时时间为了减少多个node同时超时选举的几率
        int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(task, timeout, TimeUnit.MILLISECONDS);
        return new ElectionTimeoutTask(scheduledFuture);
    }

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {

        ScheduledFuture<?> scheduledFuture = 
            this.scheduledExecutorService.scheduleWithFixedDelay(
                task, logReplicationDelay, logReplicationInterval, TimeUnit.MILLISECONDS);

        return new LogReplicationTask(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
    }

}
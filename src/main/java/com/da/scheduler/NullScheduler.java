package com.da.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For testing purpose
 * A scheduler that does nothing
 */
public class NullScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(NullScheduler.class);

    // 创建日志复制定时器
    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        logger.debug("schedule log replication task");
        return LogReplicationTask.NONE;
    }

    // 创建选举超时定时器
    @Override
    public ElectionTimeoutTask scheduleElectionTimeoutTask(Runnable task) {
        logger.debug("schedule election timeout");
        return ElectionTimeoutTask.NONE;
    }

    @Override
    public void stop() throws InterruptedException {
    }


}
package com.da.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For testing purpose
 * A scheduler that does nothing
 */
public class NullScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(NullScheduler.class);

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        logger.debug("schedule log replication task");
        return LogReplicationTask.NONE;
    }

    @Override
    public ElectionTimeoutTask scheduleElectionTimeoutTask(Runnable task) {
        logger.debug("schedule election timeout");
        return ElectionTimeoutTask.NONE;
    }

    @Override
    public void stop() throws InterruptedException {
    }


}
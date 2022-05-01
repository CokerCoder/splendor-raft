package com.da.scheduler;

public interface Scheduler {
    
    // LogReplicationTask scheduleLogReplicationTask(Runnable task);

    ElectionTimeoutTask scheduleElectionTimeoutTask(Runnable task);

    void stop() throws InterruptedException;
    
}

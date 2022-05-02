package com.da.node.roles;

import com.da.scheduler.LogReplicationTask;

public class LeaderNodeRole extends AbstractNodeRole {

    // differ from follower/candidate, leader does not have electionTimeout task
    // rather, a log replication task
    private final LogReplicationTask logReplicationTask;

    public LeaderNodeRole(int term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }

    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }

}

package com.da.node.roles;

import com.da.scheduler.LogReplicationTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderNodeRole extends AbstractNodeRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderNodeRole.class);

    // differ from follower/candidate, leader does not have electionTimeout task
    // rather, a log replication task
    private final LogReplicationTask logReplicationTask;

    public LeaderNodeRole(int term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }

    @Override
    public void cancelTimeoutOrTask() {
        LOGGER.debug("Leader replicate log task cancelled.");
        logReplicationTask.cancel();
    }


    @Override
    public String toString() {
        return "LeaderNodeRole{term=" + term + ", logReplicationTask=" + logReplicationTask + '}';
    }

}

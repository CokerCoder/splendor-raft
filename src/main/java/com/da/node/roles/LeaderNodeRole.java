package com.da.node.roles;

import com.da.node.NodeId;

public class LeaderNodeRole extends AbstractNodeRole {

    public LeaderNodeRole(int term) {
        super(RoleName.LEADER, term);
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return selfId;
    }

}

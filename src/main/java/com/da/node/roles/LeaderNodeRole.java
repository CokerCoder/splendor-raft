package com.da.node.roles;

public class LeaderNodeRole extends AbstractNodeRole {

    public LeaderNodeRole(int term) {
        super(RoleName.LEADER, term);
    }

    @Override
    public int getLeaderId(int selfId) {
        return selfId;
    }

}

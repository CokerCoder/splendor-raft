package com.da.node.roles;

import com.da.node.NodeId;

/**
 * 抽象角色类，每一个node都应该持有一个角色，该类定义了一些角色共同的特性
 */
public abstract class AbstractNodeRole {

    private final RoleName name;
    protected final int term;

    public AbstractNodeRole(RoleName name, int term) {
        this.name = name;
        this.term = term;
    }

    public RoleName getName() {
        return name;
    }

    public int getTerm() {
        return term;
    }

    public abstract NodeId getLeaderId(NodeId selfId);

}

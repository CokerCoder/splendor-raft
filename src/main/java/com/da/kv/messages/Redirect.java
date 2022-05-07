package com.da.kv.messages;

import com.da.node.NodeId;

public class Redirect {
    
    private final String leaderId;

    public Redirect(NodeId leaderId) {
        this(leaderId != null ? leaderId.getValue() : null);
    }

    public Redirect(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderId() {
        return leaderId;
    }
}

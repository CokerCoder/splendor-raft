package com.da.kv.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.da.kv.messages.GetCommand;
import com.da.kv.messages.GetCommandResponse;
import com.da.kv.messages.SetCommand;
import com.da.kv.messages.SetCommandResponse;
import com.da.log.Entry;
import com.da.log.StateMachine;
import com.da.node.Node;
import com.da.node.RaftNode;
import com.da.node.roles.AbstractNodeRole;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;
import com.da.node.roles.LeaderNodeRole;

public class KVService implements StateMachine {

    private final RaftNode node;
    // private final ConcurrentMap<String, CommandRequest<?>> pendingCommands = new ConcurrentHashMap<>();
    private Map<String, byte[]> map = new HashMap<>();

    public KVService(RaftNode node) {
        this.node = node;
        this.node.registerStateMachine(this);
    }

    @Override
    public void applyEntry(Entry entry) {
        // TODO Auto-generated method stub
        System.out.println("Applied entry");
    }
    

    public GetCommandResponse get(GetCommand command) {
        String key = command.getKey();
        byte[] value = this.map.get(key);
        return new GetCommandResponse(value);
    }

    public SetCommandResponse set(SetCommand command) {
        // first check if the current node is a leader
        AbstractNodeRole role = node.getRole();
        if (role instanceof FollowerNodeRole) {
            return new SetCommandResponse(((FollowerNodeRole) role).getLeaderId().getValue());
        }
        else if (role instanceof CandidateNodeRole) {
            return new SetCommandResponse(null);
        }

        this.map.put(command.getKey(), command.getValue());
        // apply log only if as leader
        node.appendLog(command.toBytes());

        return new SetCommandResponse();
    }
    
}

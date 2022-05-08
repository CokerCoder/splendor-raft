package com.da.kv.server;

import java.util.HashMap;
import java.util.Map;

import com.da.kv.messages.GetCommand;
import com.da.kv.messages.GetCommandResponse;
import com.da.kv.messages.SetCommand;
import com.da.kv.messages.SetCommandResponse;
import com.da.log.stateMachine.AbstractSingleThreadStateMachine;
import com.da.node.NodeId;
import com.da.node.RaftNode;
import com.da.node.roles.AbstractNodeRole;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;

public class KVService {

    private final RaftNode node;
    private Map<String, byte[]> map = new HashMap<>();

    public KVService(RaftNode node) {
        this.node = node;
        this.node.registerStateMachine(new StateMachineImpl());
    }

    public GetCommandResponse get(GetCommand command) {
        String key = command.getKey();
        byte[] value = this.map.getOrDefault(key, "".getBytes());
        return new GetCommandResponse(value);
    }

    public SetCommandResponse set(SetCommand command) {
        // first check if the current node is a leader
        AbstractNodeRole role = node.getRole();
        if (role instanceof FollowerNodeRole) {
            NodeId leaderId = ((FollowerNodeRole) role).getLeaderId();
            return new SetCommandResponse(leaderId == null ? null : leaderId.getValue());
        }
        else if (role instanceof CandidateNodeRole) {
            // when as candidate, the node has no leader, client needs to wait for a new leader
            return new SetCommandResponse(null);
        }

        this.map.put(command.getKey(), command.getValue());
        // apply log only if as leader
        this.node.appendLog(command.toBytes());

        // set success
        return new SetCommandResponse();
    }

    private class StateMachineImpl extends AbstractSingleThreadStateMachine {

        @Override
        protected void applyCommand(byte[] commandBytes) {
            SetCommand command = SetCommand.fromBytes(commandBytes);
            map.put(command.getKey(), command.getValue());
        }
    }
    
}

package com.da.node;

import com.google.common.eventbus.EventBus;

import com.da.executor.TaskExecutor;
import com.da.node.nodestatic.NodeGroup;
import com.da.rpc.RPCAdapter;
import com.da.scheduler.Scheduler;

/**
 * Node与各种service的关联类，例如在node中通过使用context.rpcAdapter来使用
 */
public class NodeContext {

    private NodeId selfId;
    private NodeGroup group; // 当前组的成员列表
    
    private RPCAdapter rpcAdapter;
    private Scheduler scheduler;
    private EventBus eventBus;
    private TaskExecutor taskExecutor;
    private NodeStore store;

    public NodeId selfId() {
        return selfId;
    }

    public void setSelfId(NodeId selfId) {
        this.selfId = selfId;
    }

    public TaskExecutor taskExecutor() {
        return taskExecutor;
    }

    public NodeGroup group() {
        return group;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public NodeStore store() {
        return store;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public RPCAdapter rpcAdapter() {
        return rpcAdapter;
    }


    public void setGroup(NodeGroup group) {
        this.group = group;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setRPCAdapter(RPCAdapter rpcAdapter) {
        this.rpcAdapter = rpcAdapter;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setStore(NodeStore store) {
        this.store = store;
    }

}

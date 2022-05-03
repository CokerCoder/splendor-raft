package com.da.node;

import java.util.Collection;
import java.util.Collections;

import com.da.executor.SingleThreadTaskExecutor;
import com.da.executor.TaskExecutor;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.nodestatic.NodeGroup;
import com.da.rpc.RPCAdapter;
import com.da.scheduler.NullScheduler;
import com.da.scheduler.Scheduler;
import com.google.common.eventbus.EventBus;

public class NodeBuilder {

    /**
     * Group.
     */
    private final NodeGroup group;

    /**
     * Self id.
     */
    private final NodeId selfId;

    /**
     * Event bus, INTERNAL.
     */
    private final EventBus eventBus;

    /**
     * Scheduler, INTERNAL.
     */
    private Scheduler scheduler = null;

    /**
     * Connector, component to communicate between nodes, INTERNAL.
     */
    private RPCAdapter connector = null;

    /**
     * Task executor for node, INTERNAL.
     */
    private TaskExecutor taskExecutor = null;


    private NodeStore nodeStore = null;


    public NodeBuilder(NodeEndpoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }

    public NodeBuilder(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.group = new NodeGroup(endpoints, selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }


    public NodeBuilder setConnector(RPCAdapter connector) {
        this.connector = connector;
        return this;
    }


    public NodeBuilder setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public NodeBuilder setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        return this;
    }

    public NodeBuilder setNodeStore(NodeStore nodeStore) {
        this.nodeStore = nodeStore;
        return this;
    }

    public Node build() {
        return new RaftNode(buildContext());
    }


    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setStore(nodeStore != null ? nodeStore : new MemoryNodeStore());
        context.setGroup(group);
        context.setSelfId(selfId);
        context.setEventBus(eventBus);
        context.setScheduler(scheduler != null ? scheduler : new NullScheduler());
        context.setRPCAdapter(connector);
        context.setTaskExecutor(taskExecutor != null ? taskExecutor : new SingleThreadTaskExecutor());
        return context;
    }


}

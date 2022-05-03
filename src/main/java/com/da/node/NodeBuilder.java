package com.da.node;

import java.util.Collection;
import java.util.Collections;

import com.da.executor.SingleThreadTaskExecutor;
import com.da.executor.TaskExecutor;
import com.da.log.Log;
import com.da.log.entryImpl.MemoryLog;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.nodestatic.NodeGroup;
import com.da.rpc.RPCAdapter;
import com.da.scheduler.NullScheduler;
import com.da.scheduler.Scheduler;
import com.google.common.eventbus.EventBus;

public class NodeBuilder {

    /**
     * Group.集群成员
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
     * Task executor for node, INTERNAL.，主线程执行器
     */
    private TaskExecutor taskExecutor = null;

    private Log log = null;

    private NodeStore nodeStore = null;

    // 单节点构造函数
    public NodeBuilder(NodeEndpoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }

    // 多节点构造函数
    public NodeBuilder(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.group = new NodeGroup(endpoints, selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }

    // 设置通信组件
    public NodeBuilder setConnector(RPCAdapter connector) {
        this.connector = connector;
        return this;
    }

    // 设置定时器
    public NodeBuilder setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    // 设置任务执行器
    public NodeBuilder setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        return this;
    }

    public NodeBuilder setNodeStore(NodeStore nodeStore) {
        this.nodeStore = nodeStore;
        return this;
    }

    // 构建node实例
    public Node build() {
        return new RaftNode(buildContext());
    }

    //构建上下文
    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setStore(nodeStore != null ? nodeStore : new MemoryNodeStore());
        context.setGroup(group);
        context.setSelfId(selfId);
        context.setEventBus(eventBus);
        context.setScheduler(scheduler != null ? scheduler : new NullScheduler());
        context.setRPCAdapter(connector);
        context.setTaskExecutor(taskExecutor != null ? taskExecutor : new SingleThreadTaskExecutor());

        context.setLog(log != null ? log : new MemoryLog());

        return context;
    }


}

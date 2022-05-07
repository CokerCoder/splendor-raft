package com.da.node.nodestatic;

import com.da.node.NodeId;

//连接节点基本信息
public class NodeEndpoint {
    private final NodeId id;
    private final Address address;

    public NodeEndpoint(String id, String host, int port){
        this(new NodeId(id), new Address(host, port));
    }

    public NodeEndpoint(NodeId id, Address address){
        this.id=id;
        this.address = address;
    }

    public NodeId getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "NodeEndpoint{id=" + id + ", address=" + address + '}';
    }
}

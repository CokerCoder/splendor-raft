package com.da.node.nodestatic;

import com.da.node.NodeId;

import java.util.*;
import java.util.stream.Collectors;

//集群成员映射表
public class NodeGroup {
    private final NodeId selfId;
    private Map<NodeId, GroupMember> memberMap;

    //单节点构造
    NodeGroup(NodeEndpoint endpoint) {
        this(Collections.singleton(endpoint), endpoint.getId());
    }

    //多节点构造
    NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId){
        this.memberMap = buildMemberMap(endpoints);
        this.selfId = selfId;
    }

    private Map<NodeId, GroupMember> buildMemberMap(Collection<NodeEndpoint> endpoints) {
        Map<NodeId, GroupMember> map=new HashMap<>();
        for(NodeEndpoint endpoint :endpoints){
            map.put(endpoint.getId(), new GroupMember(endpoint));
        }
        if(map.isEmpty()){
            throw new IllegalArgumentException("endpoints is empty!");
        }
        return map;
    }

    //按照节点ID查找成员，找不到抛出错误
    GroupMember findMember(NodeId id){
        GroupMember member = getMember(id);
        if(member==null){
            throw new IllegalArgumentException("no such node"+id);
        }
        return member;
    }

    //按照节点ID查找成员，找不到返回空,例如集群刚启动时Follower不会有有效的leaderId
    public GroupMember getMember(NodeId id){
        return memberMap.get(id);
    }

    public Collection<GroupMember> listReplicationTarget(){
        return memberMap.values().stream().filter(
                m->!m.idEquals(selfId)).collect(Collectors.toList());

    }

    public Set<NodeEndpoint> listEndPointExceptSelf(){
        Set<NodeEndpoint> endpoints = new HashSet<>();
        for(GroupMember member:memberMap.values()){
            //判断是否为当前节点
            if(!member.getId().equals(selfId)){
                endpoints.add(member.getEndpoint());
            }
        }
        return endpoints;
    }

    public int getCount() {
        return memberMap.size();
    }

}

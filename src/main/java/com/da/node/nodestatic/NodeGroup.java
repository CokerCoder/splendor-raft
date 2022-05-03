package com.da.node.nodestatic;

import com.da.node.NodeId;

import javax.annotation.Nonnull;
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
    public NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId){
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


    int getMatchIndexOfMajor() {
        List<NodeMatchIndex> matchIndices = new ArrayList<>();
        for (GroupMember member : memberMap.values()) {
            if (!member.idEquals(selfId)) {
                matchIndices.add(new NodeMatchIndex(member.getId(), member.getMatchIndex()));
            }
        }
        int count = matchIndices.size();
        // 没有节点的情况
        if (count == 0) {
            throw new IllegalStateException("standalone or no major node");
        }
        Collections.sort(matchIndices);
        logger.debug("match indices {}", matchIndices);
        // 取排序后的中间位置的matchIndex
        return matchIndices.get(count / 2).getMatchIndex();
    }


    /**
     * Node match index.
     *
     * @see NodeGroup#getMatchIndexOfMajor()
     */
    private static class NodeMatchIndex implements Comparable<NodeMatchIndex> {

        private final NodeId nodeId;
        private final int matchIndex;

        NodeMatchIndex(NodeId nodeId, int matchIndex) {
            this.nodeId = nodeId;
            this.matchIndex = matchIndex;
        }

        int getMatchIndex() {
            return matchIndex;
        }

        @Override
        public int compareTo(@Nonnull NodeMatchIndex o) {
            return -Integer.compare(o.matchIndex, this.matchIndex);
        }

        @Override
        public String toString() {
            return "<" + nodeId + ", " + matchIndex + ">";
        }

    }

}

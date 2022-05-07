
package com.da.entity;
import com.da.log.Entry;
import com.da.node.NodeId;

import java.util.ArrayList;
import java.util.List;

public class AppendEntriesRpc {

    /** 当前的任期号 **/
    private int term;

    /** 领导人ID */
    private NodeId leaderId;

    /** 紧邻新日志前的那个日志条目的索引 */
    private int prevLogIndex;

    /** 紧邻新日志前的那个日志条目的任期号 */
    private int prevLogTerm;

    /** 准备存储的日志条目（表示心跳时为空；可以一次性发送多个） */
    private List<Entry> entries = new ArrayList<>();

    /** 领导人已经提交的日志的索引值  */
    private int leaderCommit;

    public int getTerm() {
        return term;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public List<Entry> getEntries() {
        return entries;
    }


    public void setTerm(int term) {
        this.term = term;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public void setPrevLogIndex(int prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public void setPrevLogTerm(int prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public int getLastEntryIndex() {
        return this.entries.isEmpty() ? this.prevLogIndex : this.entries.get(this.entries.size() - 1).getIndex();
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
    
    @Override
    public String toString() {
        return "AppendEntriesRpc{" +
                "entries.size=" + entries.size() +
                ", leaderCommit=" + leaderCommit +
                ", leaderId=" + leaderId +
                ", prevLogIndex=" + prevLogIndex +
                ", prevLogTerm=" + prevLogTerm +
                ", term=" + term +
                '}';
    }

}

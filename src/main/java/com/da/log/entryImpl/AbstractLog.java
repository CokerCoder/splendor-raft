package com.da.log.entryImpl;

import com.da.entity.AppendEntriesRpc;
import com.da.log.*;
import com.da.log.entrySequence.EntrySequence;
import com.da.node.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public abstract class AbstractLog implements Log {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLog.class);
    protected EntrySequence entrySequence;
    protected int commitIndex = 0;

    //获取最后一条日志元信息
    public EntryMeta getLastEntryMeta(){
        if(entrySequence.isEmpty()){
            return new EntryMeta(Entry.KIND_NO_OP, 0, 0);
        }
        return entrySequence.getLastEntry().getMeta();
    }

    //创建AppendEntries消息
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId,
                                                   int nextIndex, int maxEntries){
        int nextLogIndex = entrySequence.getNextLogIndex();
        if(nextIndex>nextLogIndex){
            throw new IllegalArgumentException("illegal next index"+nextIndex);
        }
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(commitIndex); //?
        //设置前一条日志的元信息，可能为null
        Entry entry = entrySequence.getEntry(nextIndex-1);
        if(entry!=null){
            rpc.setPrevLogIndex(entry.getIndex());
            rpc.setPrevLogTerm(entry.getTerm());
        }
        //设置entries
        if(!entrySequence.isEmpty()){
            int maxIndex = (maxEntries==ALL_ENTRIES?
                            nextLogIndex:Math.min(nextLogIndex, nextIndex+maxEntries));
            rpc.setEntries(entrySequence.subList(nextIndex, maxIndex));
        }
        return rpc;
    }

    //用于RequestVote中投票检查isNewerThan
    public boolean isNewerThan(int lastLogIndex, int lastLogTerm){
        EntryMeta lastEntryMeta = getLastEntryMeta();
        logger.debug("last entry({}, {}), candidate({}, {})",
                lastLogIndex, lastLogTerm);
        return lastEntryMeta.getTerm() > lastLogTerm || lastEntryMeta.getIndex()>lastLogIndex;
    }

    //追加NO-OP日志
    public NoOpEntry appendEntry(int term){
        NoOpEntry entry = new NoOpEntry(entrySequence.getNextLogIndex(), term);
        entrySequence.append(entry);
        return entry;
    }

    //追加一般日志
    public GeneralEntry appendEntry(int term, byte[] command){
        GeneralEntry entry = new GeneralEntry(entrySequence.getNextLogIndex(), term, command);
        entrySequence.append(entry);
        return entry;
    }

    //从leader追加日志
    //追加前需移除不一致的日志
    //移除前从最后一条匹配的日志条目开始，之后所有冲突日志都被移除
    public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm,
                                          List<Entry> leaderEntries){
        //检查前一条日志是否匹配
        if(!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)){
            return false;
        }
        //Leader节点传递过来的日志条目为空
        if(leaderEntries.isEmpty()){
            return true;
        }
        //移除冲突的日志条目并返回接下来要追加的日志条目（如果还有的话）
        EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(leaderEntries));

        //仅追加日志
        appendEntriesFromLeader(newEntries);
        return true;
    }

    //上面方法中prevLogIndex不一定对应最后一条日志，Leader节点会从后往前找到第一个匹配的日志
    //此处需要随机访问EntrySequence来获取日志条目元信息
    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm){
        //检查指定索引的日志条目
        EntryMeta meta = entrySequence.getEntryMeta(prevLogIndex);
        //日志不存在
        if(meta==null){
            logger.debug("previous log {} not found", prevLogIndex);
            return false;
        }
        int term = meta.getTerm();
        if(term != prevLogTerm){
            logger.debug("different term of previous log, local {}, remote {}", term,prevLogTerm);
            return false;
        }
        return true;
    }

    private static class EntrySequenceView implements Iterable<Entry>{
        private final List<Entry> entries;
        private int firstLogIndex = -1;
        private int lastLogIndex = -1;

        EntrySequenceView(List<Entry> entries){
            this.entries = entries;
            if(!entries.isEmpty()){
                firstLogIndex = entries.get(0).getIndex();
                lastLogIndex = entries.get(entries.size()-1).getIndex();
            }
        }

        //获取指定位置日志条目
        Entry get(int index){
            if(entries.isEmpty() || index<firstLogIndex || index>lastLogIndex){
                return null;
            }
            return entries.get(index-firstLogIndex);
        }

        boolean isEmpty(){
            return entries.isEmpty();
        }

        int getFirstLogIndex(){
            return firstLogIndex;
        }

        int getLastLogIndex(){
            return lastLogIndex;
        }

        //获取子视图
        EntrySequenceView subView(int fromIndex){
            if(entries.isEmpty() || fromIndex>lastLogIndex){
                return new EntrySequenceView(Collections.emptyList());
            }
            return new EntrySequenceView(entries.subList(fromIndex-firstLogIndex, entries.size()));
        }

        //遍历
        @Override
        @Nonnull
        public Iterator<Entry> iterator(){
            return entries.iterator();
        }
    }

    private EntrySequenceView removeUnmatchedLog(EntrySequenceView leaderEntries){
        assert !leaderEntries.isEmpty();
        int firstUnmatched = findFirstUnmatchedLog(leaderEntries);
        if(firstUnmatched<0){
            //都匹配
            return new EntrySequenceView(Collections.emptyList());
        }
        //移除不匹配的日志索引开始的所有日志
        removeEntriesAfter(firstUnmatched-1);
        //返回之后追加的日志条目
        return leaderEntries.subView(firstUnmatched);
    }

    private int findFirstUnmatchedLog(EntrySequenceView leaderEntries) {
        int logIndex;
        EntryMeta followerEntryMeta;
        // 从后往前遍历leaderEntries
        for(Entry leadeEntry: leaderEntries){
            logIndex = leadeEntry.getIndex();
            //按照索引查找日志条目元信息
            followerEntryMeta = entrySequence.getEntryMeta(logIndex);
            //日志不存在或者term不一致
            if(followerEntryMeta==null || followerEntryMeta.getTerm()!=leadeEntry.getTerm()){
                return logIndex;
            }
        }

        //否则没有不一致的日志条目
        return -1;
    }

    private void removeEntriesAfter(int index){
        if(entrySequence.isEmpty() || index>= entrySequence.getLastLogIndex()){
            return;
        }
        //注意如果此处移除了已经应用的日志则需从头开始构建状态机
        logger.debug("remove entries after {}", index);
        entrySequence.removeAfter(index);
        if(index<commitIndex){
            commitIndex = index;
        }
    }

    private void appendEntriesFromLeader(EntrySequenceView leaderEntries){
        if(leaderEntries.isEmpty()){
            return;
        }
        logger.debug("append entries from leader from {} to {}", leaderEntries.getFirstLogIndex(), leaderEntries.getLastLogIndex());
        for(Entry leaderEntry: leaderEntries){
            entrySequence.append(leaderEntry);
        }
    }

    public void advanceCommitIndex(int newCommitIndex, int currenTerm){
        if(!validateNewCommitIndex(newCommitIndex, currenTerm)){
            return;
        }
        logger.debug("advance commit index from {} to {}", commitIndex, newCommitIndex);
        entrySequence.commit(newCommitIndex);
        commitIndex = newCommitIndex;
    }

    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm){
        //小于当前的commitIndex
        if(newCommitIndex<=entrySequence.getCommitIndex()){
            return false;
        }
        EntryMeta meta = entrySequence.getEntryMeta(newCommitIndex);
        if(meta==null){
            logger.debug("log of new commit index {} not found", newCommitIndex);
            return false;
        }

        //日志条目的term必须是当前term，才可推进commitIndex
        if(meta.getTerm()!=currentTerm){
            logger.debug("log term of new commit index != current term {}!={}",
                    meta.getTerm(), currentTerm);
            return false;
        }
        return true;
    }

    @Override
    public int getNextIndex() {
        return entrySequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public void close() {
        entrySequence.close();
    }
}

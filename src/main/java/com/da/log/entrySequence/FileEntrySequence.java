package com.da.log.entrySequence;

import com.da.log.Entry;
import com.da.log.EntryMeta;
import com.da.log.LogException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//EntriesFile,EntryIndexFile 和 日志缓冲pendingEntries 构成了FileEntrySequence
public class FileEntrySequence extends AbstractEntrySequence{
    private final EntryFactory entryFactory = new EntryFactory();
    // 数据库表文件，使用RandomAccessFile随机访问
    private final EntriesFile entriesFile;
    // 数据库表的索引
    private final EntryIndexFile entryIndexFile;
    private final LinkedList<Entry> pendingEntries = new LinkedList<>();

    //Raft算法中初始commitIndex为0
    private int commitIndex = 0;
    public FileEntrySequence(LogDir logDir, int logIndexOffset){
        super(logIndexOffset);
        try{
            this.entriesFile = new EntriesFile(logDir.getEntriesFile());
            this.entryIndexFile = new EntryIndexFile(logDir.getEntryOffsetIndexFile());
            initialize();
        }catch (IOException e){
            throw new IllegalStateException("Fail to open entries file or entry index file",e);
        }
    }

    public FileEntrySequence(EntriesFile entriesFile, EntryIndexFile entryIndexFile, int logIndexOffset){
        super(logIndexOffset);
        this.entryIndexFile = entryIndexFile;
        this.entriesFile = entriesFile;
        initialize();
    }

    private void initialize(){
        if(entryIndexFile.isEmpty()){
            return;
        }
        logIndexOffset = entryIndexFile.getMinEntryIndex();
        nextLogIndex = entryIndexFile.getMaxEntryIndex()+1;
    }

    public int getCommitIndex(){
        return commitIndex;
    }


    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        //结果分别来自文件以及缓冲两部分
        List<Entry> result = new ArrayList<>();
        //从文件
        if(!entryIndexFile.isEmpty()&&fromIndex<=entryIndexFile.getMaxEntryIndex()){
            int maxIndex = Math.min(entryIndexFile.getMaxEntryIndex()+1, toIndex);
            for(int i=fromIndex; i<maxIndex; i++){
                result.add(getEntryInFile(i));
            }
        }
        //从日志缓冲
        if(!pendingEntries.isEmpty() && toIndex>pendingEntries.getFirst().getIndex()){
            Iterator<Entry> iterator = pendingEntries.iterator();
            Entry entry;
            int index;
            while(iterator.hasNext()){
                entry = iterator.next();
                index=entry.getIndex();
                if(index>=toIndex) {
                    break;
                }
                if(index>=fromIndex){
                    result.add(entry);
                }
            }
        }
        return result;
    }

    // 获取指定位置的日志条目
    @Override
    protected Entry doGetEntry(int index) {
        if(!pendingEntries.isEmpty()){
            int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
            if(index>=firstPendingEntryIndex){
                return pendingEntries.get(index-firstPendingEntryIndex);
            }
        }
        assert  !entryIndexFile.isEmpty();
        return getEntryInFile(index);
    }

    // 获取日志元信息
    public EntryMeta getEntryMeta(int index){
        if(!isEntryPresent(index)){
            return null;
        }
        if(!pendingEntries.isEmpty()){
            int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
            if(index>=firstPendingEntryIndex){
                return pendingEntries.get(index-firstPendingEntryIndex).getMeta();
            }
        }
        return entryIndexFile.get(index).toEntryMeta();
    }

    // 按照索引获取文件中的日志条目
    private Entry getEntryInFile(int index){
        long offset = entryIndexFile.getOffset(index);
        try{
            return entriesFile.loadEntry(offset, entryFactory);
        }catch (IOException e){
            throw new IllegalStateException("fail to load entry "+ index, e);
        }
    }
    // 获取最后一条日志
    public Entry getLastEntry(){
        if(isEmpty()){
            return null;
        }
        if(!pendingEntries.isEmpty()){
            return pendingEntries.getLast();
        }
        assert !entryIndexFile.isEmpty();
        return getEntryInFile(entryIndexFile.getMaxEntryIndex());
    }

    @Override
    protected void doAppend(Entry entry) {
        pendingEntries.add(entry);
    }

    // 需要判断移除的日志索引是否在日志绥冲中。如果在， 那么就只需从后往前移除日志绥冲中的部分日志即可, 否则需要整体消除 日志缓冲 ，
    // 文件需要裁剪。
    @Override
    protected void doRemoveAfter(int index) {
        if(!pendingEntries.isEmpty() && index>=pendingEntries.getFirst().getIndex()-1){
            //移除指定数量的日志条目
            //循环方向从小到大，从后往前
            for(int i=index+1; i<=doGetLastLogIndex(); i++){
                pendingEntries.removeLast();
            }
            nextLogIndex = index+1;
            return;
        }
        try{
            if(index>=doGetFirstLogIndex()){
                //索引比日志缓冲的第一条日志小
                pendingEntries.clear();
                entriesFile.truncate(entryIndexFile.getOffset(index+1));
                entryIndexFile.removeAfter(index);
                nextLogIndex = index+1;
                commitIndex = index;
            }else {
                //索引比第一条日志的索引小
                pendingEntries.clear();
                entriesFile.clear();
                entryIndexFile.clear();
                nextLogIndex = logIndexOffset;
                commitIndex = logIndexOffset-1;
            }
        }catch (IOException e){
            throw new IllegalStateException(e);
        }
    }

    public void commit(int index){
        if(index<commitIndex){
            throw new IllegalArgumentException("commit index<"+commitIndex);
        }
        if(index==commitIndex){
            return;
        }
        //如果commitIndex在文件内，则只更新commitIndex
        if(!entryIndexFile.isEmpty() && index<=entryIndexFile.getMaxEntryIndex()){
            commitIndex = index;
            return;
        }
        //检查commitIndex是否在日志缓冲的区间里
        if(pendingEntries.isEmpty() ||
                pendingEntries.getFirst().getIndex()>index ||
                pendingEntries.getLast().getIndex()<index){
            throw new IllegalArgumentException("no entry to commit or commit index exceed");
        }
        long offset;
        Entry entry = null;
        try{
            for(int i=pendingEntries.getFirst().getIndex(); i<=index;i++){
                entry=pendingEntries.removeFirst();
                offset=entriesFile.appendEntry(entry);
                entryIndexFile.appenEntryIndex(i,offset,entry.getKind(),entry.getTerm());
                commitIndex = i;
            }
        }catch (IOException e){
            throw new IllegalStateException("fail to commit entry "+ entry, e);
        }
    }

    @Override
    public void close() {

    }
}

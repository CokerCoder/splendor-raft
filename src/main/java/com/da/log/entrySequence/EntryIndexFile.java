package com.da.log.entrySequence;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntryIndexFile implements Iterable<EntryIndexItem>{
    //最大条目索引偏移
    private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;
    //单条日志条目元信息长度
    private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

    private final SeekableFile seekableFile;
    private int entryIndexCount;
    private int minEntryIndex;
    private int maxEntryIndex;
    private Map<Integer, EntryIndexItem> entryIndexMap = new HashMap<>();

    //构造函数-普通文件
    public EntryIndexFile(File file) throws IOException{
        this(new RandomAccessFileAdapter(file));
    }

    public EntryIndexFile(SeekableFile seekableFile) throws  IOException{
        this.seekableFile = seekableFile;
        load();
    }

    private void load() throws IOException{
        if(seekableFile.size()==0L){
            entryIndexCount=0;
            return;
        }
        minEntryIndex = seekableFile.readInt();
        maxEntryIndex = seekableFile.readInt();
        updateEntryIndexCount();
        //逐条加载
        long offset;
        int kind;
        int term;
        for(int i=minEntryIndex; i<=maxEntryIndex; i++){
            offset=seekableFile.readLong();
            kind= seekableFile.readInt();
            term = seekableFile.readInt();
            entryIndexMap.put(i, new EntryIndexItem(i,offset,kind,term));
        }
    }

    private void updateEntryIndexCount(){
        entryIndexCount = maxEntryIndex-minEntryIndex +1;
    }

    //追加日志条目元信息
    public void appenEntryIndex(int index, long offset,int kind, int term) throws IOException{
        if(seekableFile.size()==0L){
            seekableFile.writeInt(index);
            minEntryIndex = index;
        }else{
            if(index!=maxEntryIndex+1){
                throw new IllegalArgumentException(
                        "index mush be "+ (maxEntryIndex+1)+ " but was "+index
                );
            }
            seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        }
        seekableFile.writeInt(index);
        maxEntryIndex = index;
        updateEntryIndexCount();
        //移动到文件最后
        seekableFile.seek(getOffsetOfEntryIndexItem(index));
        seekableFile.writeLong(offset);
        seekableFile.writeInt(kind);
        seekableFile.writeInt(term);
        entryIndexMap.put(index, new EntryIndexItem(index, offset, kind, term));
    }

    private long getOffsetOfEntryIndexItem(int index){
        return (index-minEntryIndex)*LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES*2;
    }

    public void clear() throws IOException{
        seekableFile.truncate(0L);
        entryIndexCount=0;
        entryIndexMap.clear();
    }

    //移除某个索引后的数据
    public void removeAfter(int newMaxEntryIndex ) throws IOException{
        if(isEmpty() || newMaxEntryIndex>=maxEntryIndex){
            return;
        }
        if(newMaxEntryIndex<minEntryIndex){
            clear();
            return;
        }
        //修改maxEntryIndex
        seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        seekableFile.writeInt(newMaxEntryIndex);
        //裁剪文件
        seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex+1));
        //移除缓存中元信息
        for(int i=newMaxEntryIndex+1; i<=maxEntryIndex; i++){
            entryIndexMap.remove(i);
        }
        maxEntryIndex = newMaxEntryIndex;
        entryIndexCount = newMaxEntryIndex+1;
    }


    //Iterable接口,从外部可以遍历EntryIndexFile所有日志条目元信息
    public Iterator<EntryIndexItem> iterator(){
        if(isEmpty()){
            return Collections.emptyIterator();
        }
        return new EntryIndexIterator(entryIndexCount, minEntryIndex);
    }

    public int getMaxEntryIndex() {
        return maxEntryIndex;
    }

    public int getMinEntryIndex() {
        return minEntryIndex;
    }

    //自加
    public boolean isEmpty() {
        return entryIndexCount==0;
    }

    //自加
    public EntryIndexItem get(int index) {
        return entryIndexMap.get(index);
    }

    //自加
    public long getOffset(int index) {
        return entryIndexMap.get(index).getOffset();
    }

    private class EntryIndexIterator implements Iterator<EntryIndexItem>{
        private final int entryIndexCount;
        private int currentEntryIndex;

        EntryIndexIterator(int entryIndexCount, int minEntryIndex){
            this.entryIndexCount = entryIndexCount;
            this.currentEntryIndex = minEntryIndex;
        }

        public boolean hasNext(){
            checkModification();
            return currentEntryIndex<=maxEntryIndex;
        }

        private void checkModification(){
            if(this.entryIndexCount!=EntryIndexFile.this.entryIndexCount){
                throw new IllegalStateException("entry index count changed!");
            }
        }

        public EntryIndexItem next(){
            checkModification();
            return entryIndexMap.get(currentEntryIndex);
        }
    }
}

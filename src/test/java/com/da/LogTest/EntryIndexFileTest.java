package com.da.LogTest;

import com.da.log.entrySequence.ByteArraySeekableFile;
import com.da.log.entrySequence.EntryIndexFile;
import com.da.log.entrySequence.EntryIndexItem;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

public class EntryIndexFileTest {
    private ByteArraySeekableFile makeEntryIndexFileContent(int minEntryIndex, int maxEntryIndex) throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        seekableFile.writeInt(minEntryIndex);
        seekableFile.writeInt(maxEntryIndex);
        for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
            seekableFile.writeLong(10L * i); // offset
            seekableFile.writeInt(1); // kind
            seekableFile.writeInt(i); // term
        }
        seekableFile.seek(0L);
        return seekableFile;
    }

    @Test
    public void testLoad() throws IOException {
        ByteArraySeekableFile seekableFile = makeEntryIndexFileContent(3, 4);

        EntryIndexFile file = new EntryIndexFile(seekableFile);
        Assert.assertEquals(3, file.getMinEntryIndex());
        Assert.assertEquals(4, file.getMaxEntryIndex());
        Assert.assertEquals(2, file.getEntryIndexCount());

        EntryIndexItem item = file.get(3);
        Assert.assertNotNull(item);
        Assert.assertEquals(30L, item.getOffset());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(3, item.getTerm());

        item = file.get(4);
        Assert.assertNotNull(item);
        Assert.assertEquals(40L, item.getOffset());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(4, item.getTerm());
    }

    @Test
    public void testAppendEntryIndex() throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        EntryIndexFile file = new EntryIndexFile(seekableFile);

        // append when empty
        file.appendEntryIndex(10, 100L, 1, 2);
        Assert.assertEquals(1, file.getEntryIndexCount());
        Assert.assertEquals(10, file.getMinEntryIndex());
        Assert.assertEquals(10, file.getMaxEntryIndex());

        // check file content
        seekableFile.seek(0L);
        Assert.assertEquals(10, seekableFile.readInt()); // min entry index
        Assert.assertEquals(10, seekableFile.readInt()); // max entry index
        Assert.assertEquals(100L, seekableFile.readLong()); // offset
        Assert.assertEquals(1, seekableFile.readInt()); // kind
        Assert.assertEquals(2, seekableFile.readInt()); // term

        EntryIndexItem item = file.get(10);
        Assert.assertNotNull(item);
        Assert.assertEquals(100L, item.getOffset());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(2, item.getTerm());

        // append when not empty
        file.appendEntryIndex(11, 200L, 1, 2);
        Assert.assertEquals(2, file.getEntryIndexCount());
        Assert.assertEquals(10, file.getMinEntryIndex());
        Assert.assertEquals(11, file.getMaxEntryIndex());

        // check file content
        seekableFile.seek(24L); // skip min/max and first entry index
        Assert.assertEquals(200L, seekableFile.readLong()); // offset
        Assert.assertEquals(1, seekableFile.readInt()); // kind
        Assert.assertEquals(2, seekableFile.readInt()); // term
    }

    @Test
    public void testClear() throws IOException {
        ByteArraySeekableFile seekableFile = makeEntryIndexFileContent(5, 6);
        EntryIndexFile file = new EntryIndexFile(seekableFile);
        Assert.assertFalse(file.isEmpty());
        file.clear();
        Assert.assertTrue(file.isEmpty());
        Assert.assertEquals(0, file.getEntryIndexCount());
        Assert.assertEquals(0L, seekableFile.size());
    }

    @Test
    public void testRemoveAfter() throws IOException {
        ByteArraySeekableFile seekableFile = makeEntryIndexFileContent(5, 6);
        long oldSize = seekableFile.size();
        EntryIndexFile file = new EntryIndexFile(seekableFile);
        file.removeAfter(6);
        Assert.assertEquals(5, file.getMinEntryIndex());
        Assert.assertEquals(6, file.getMaxEntryIndex());
        Assert.assertEquals(oldSize, seekableFile.size());
        Assert.assertEquals(2, file.getEntryIndexCount());
    }

    @Test
    public void testGet() throws IOException {
        EntryIndexFile file = new EntryIndexFile(makeEntryIndexFileContent(3, 4));
        EntryIndexItem item = file.get(3);
        Assert.assertNotNull(item);
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(3, item.getTerm());
    }

    @Test
    public void testIterator() throws IOException {
        EntryIndexFile file = new EntryIndexFile(makeEntryIndexFileContent(3, 4));
        Iterator<EntryIndexItem> iterator = file.iterator();
        Assert.assertTrue(iterator.hasNext());
        EntryIndexItem item = iterator.next();
        Assert.assertEquals(3, item.getIndex());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(3, item.getTerm());
        Assert.assertTrue(iterator.hasNext());
        item = iterator.next();
        Assert.assertEquals(4, item.getIndex());
        Assert.assertFalse(iterator.hasNext());
    }




}

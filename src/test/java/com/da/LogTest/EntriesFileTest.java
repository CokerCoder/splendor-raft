package com.da.LogTest;

import com.da.log.Entry;
import com.da.log.GeneralEntry;
import com.da.log.NoOpEntry;
import com.da.log.entrySequence.ByteArraySeekableFile;
import com.da.log.entrySequence.EntriesFile;
import com.da.log.entrySequence.EntryFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class EntriesFileTest {
    // 测试追加日志条目
    @Test
    public void testAppendEntry() throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        EntriesFile file = new EntriesFile(seekableFile);
        Assert.assertEquals(0L, file.appendEntry(new NoOpEntry(2, 3)));

        seekableFile.seek(0);
        Assert.assertEquals(Entry.KIND_NO_OP, seekableFile.readInt());
        Assert.assertEquals(2, seekableFile.readInt()); // index
        Assert.assertEquals(3, seekableFile.readInt()); // term
        Assert.assertEquals(0, seekableFile.readInt()); // command bytes length

        byte[] commandBytes = "test".getBytes();
        Assert.assertEquals(16L, file.appendEntry(new GeneralEntry(3, 3, commandBytes)));
        seekableFile.seek(16L);
        Assert.assertEquals(Entry.KIND_GENERAL, seekableFile.readInt());
        Assert.assertEquals(3, seekableFile.readInt()); // index
        Assert.assertEquals(3, seekableFile.readInt()); // term
        Assert.assertEquals(4, seekableFile.readInt()); // command bytes length
        byte[] buffer = new byte[4];
        seekableFile.read(buffer);
        Assert.assertArrayEquals(commandBytes, buffer);
    }
    // 测试加载日志条目
    @Test
    public void testLoadEntry() throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        EntriesFile file = new EntriesFile(seekableFile);
        Assert.assertEquals(0L, file.appendEntry(new NoOpEntry(2, 3)));
        Assert.assertEquals(16L, file.appendEntry(new GeneralEntry(3, 3, "test".getBytes())));
        Assert.assertEquals(36L, file.appendEntry(new GeneralEntry(4, 3, "foo".getBytes())));

        EntryFactory factory = new EntryFactory();

        Entry entry = file.loadEntry(0L, factory);
        Assert.assertEquals(Entry.KIND_NO_OP, entry.getKind());
        Assert.assertEquals(2, entry.getIndex());
        Assert.assertEquals(3, entry.getTerm());

        entry = file.loadEntry(36L, factory);
        Assert.assertEquals(Entry.KIND_GENERAL, entry.getKind());
        Assert.assertEquals(4, entry.getIndex());
        Assert.assertEquals(3, entry.getTerm());
        Assert.assertArrayEquals("foo".getBytes(), entry.getCommandBytes());
    }
    // 测试剪裁
    @Test
    public void testTruncate() throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        EntriesFile file = new EntriesFile(seekableFile);
        file.appendEntry(new NoOpEntry(2, 3));
        Assert.assertTrue(seekableFile.size() > 0);
        file.truncate(0L);
        Assert.assertEquals(0L, seekableFile.size());
    }

}

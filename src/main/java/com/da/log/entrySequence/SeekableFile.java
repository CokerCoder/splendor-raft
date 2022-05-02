package com.da.log.entrySequence;

import java.io.IOException;
import java.io.InputStream;

public interface SeekableFile {
    long position() throws IOException;
    void seek(long position) throws IOException ;
    void writeInt(int i)  throws IOException;
    void writeLong(long l)  throws IOException;
    void write(byte[] b) throws IOException ;
    int readInt() throws IOException ;
    long readLong()  throws IOException;
    int read(byte[] b) throws IOException ;
    long size() throws IOException ;
    void truncate(long size ) throws IOException ; //裁剪到指定大小
    InputStream inputStream(long start)  throws IOException;  //获取从指定位置开始的输入流,用于日志快照
    void flush()  throws IOException;   //强制输出到磁盘
    void close()  throws IOException;
}

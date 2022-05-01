package com.da.log.entrySequence;

import java.io.*;

public class RandomAccessFileAdapter implements SeekableFile{
    private final File file;
    private final RandomAccessFile randomAccessFile;

    public RandomAccessFileAdapter(File file) throws FileNotFoundException{
        this(file, "rw");
    }

    public RandomAccessFileAdapter(File file, String mode) throws FileNotFoundException{
        this.file = file;
        randomAccessFile = new RandomAccessFile(file, mode);
    }

    public void seek(long position) throws IOException{
        randomAccessFile.seek(position);
    }

    public void writeInt(int i)  throws IOException{
        randomAccessFile.write(i);
    }
    public void writeLong(long l)  throws IOException{
        randomAccessFile.writeLong(l);
    }
    public void write(byte[] b) throws IOException {
        randomAccessFile.write(b);
    }
    public int readInt() throws IOException {
        return randomAccessFile.readInt();
    }
    public long readLong() throws IOException{
        return randomAccessFile.readLong();
    }

    public int read(byte[] b) throws IOException {
        return randomAccessFile.read(b);
    }
    public long size() throws IOException {
        return randomAccessFile.length();
    }

    //裁剪到指定大小
    public void truncate(long size ) throws IOException {
        randomAccessFile.setLength(size);
    }

    //获取从指定位置开始的输入流
    public InputStream inputStream(long start)  throws IOException{
        FileInputStream input = new FileInputStream(file);
        if(start>0){
            input.skip(start);
        }
        return input;
    }

    //获得当前位置
    public long position() throws IOException{
        return randomAccessFile.getFilePointer();
    }

    //强制输出到磁盘
    public void flush()  throws IOException{}


    public void close()  throws IOException{
        randomAccessFile.close();
    }
}

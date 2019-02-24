package com.ren.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Socket {
    
    public static int INIT_ID = 0;
    
    private final int id;
    private final SocketChannel socketChannel;
    private boolean endOfStreamReached = false; // socketChannel 是否到达流的尾部
    
    private DataWriter writer;
    private DataReader reader;
    
    public Socket(int id, SocketChannel socketChannel) {
        this.id = id;
        this.socketChannel = socketChannel;
    }
    
    
    /**
     * read the data into buf, until no data at that time or reach end-of-stream
     * 如果channel 过来的数据过多? buf 的容量不够, 只读buf 容量大小的数据.
     * 如果出现错误,返回-1
     * @param buf
     */
    public int read(ByteBuffer buf) {
        try {
            int readBytes = socketChannel.read(buf);
            int total = readBytes;
            while (readBytes > 0) {
                readBytes = socketChannel.read(buf);
                total += readBytes;
            }
            if (readBytes == -1) {
                endOfStreamReached = true;
            }
            return total;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * write the data, 可能buf里面的数据还没有完全写完就会退出
     * @param buf
     * @return
     */
    public int write(ByteBuffer buf) {
        int bytesWritten = 0;
        int total = 0;
        try {
            bytesWritten = socketChannel.write(buf);
            total = bytesWritten;
            while (bytesWritten > 0 && buf.hasRemaining()) {
                bytesWritten = socketChannel.write(buf);
                total += bytesWritten;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }
    
    
    public int getId() {
        return id;
    }
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    public boolean isEndOfStreamReached() {
        return endOfStreamReached;
    }


    public DataWriter getWriter() {
        return writer;
    }


    public void setWriter(DataWriter writer) {
        this.writer = writer;
    }


    public DataReader getReader() {
        return reader;
    }


    public void setReader(DataReader reader) {
        this.reader = reader;
    }
    
    
}

package com.ren.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;


public class Socket {
    private static AtomicLong INIT_ID = new AtomicLong(0);
    public long socketId;

    public final SocketChannel  socketChannel;
    public final IMessageReader messageReader;
    public final MessageWriter  messageWriter;

    public boolean endOfStreamReached = false;
    public boolean willClose = false;
    public Socket(SocketChannel socketChannel, MessageBuffer readBuf, MessageBuffer wBuf, 
            IMessageReaderFactory readFactory) {
        this.socketChannel = socketChannel;
        
        messageWriter = new MessageWriter(wBuf);
        
        messageReader = readFactory.createMessageReader();
        
        messageReader.init(readBuf);
        
        this.socketId = INIT_ID.getAndIncrement();
    }

    public int read(ByteBuffer byteBuffer) throws IOException {
        int bytesRead = this.socketChannel.read(byteBuffer);
        int totalBytesRead = bytesRead;

        while(bytesRead > 0){
            bytesRead = this.socketChannel.read(byteBuffer);
            totalBytesRead += bytesRead;
        }
        if(bytesRead == -1){
            this.endOfStreamReached = true;
        }
        if (bytesRead == 0) {
            this.willClose = true;
        }
        return totalBytesRead;
    }

    public int write(ByteBuffer byteBuffer) throws IOException{
        int bytesWritten      = this.socketChannel.write(byteBuffer);
        int totalBytesWritten = bytesWritten;

        while(bytesWritten > 0 && byteBuffer.hasRemaining()){
            bytesWritten = this.socketChannel.write(byteBuffer);
            totalBytesWritten += bytesWritten;
        }

        return totalBytesWritten;
    }


}

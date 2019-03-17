package com.ren.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageWriter {
    
    private List<Message> writeQueue   = new ArrayList<>();
    private Message  messageInProgress = null;
    private int      bytesWritten      =    0;
    
    private MessageBuffer writeBuf;
    
    public MessageWriter(MessageBuffer writeBuf) {
        this.writeBuf = writeBuf;
    }
    
    public void enqueue(Message message) {
        if(this.messageInProgress == null){
            this.messageInProgress = message;
        } else {
            this.writeQueue.add(message);
        }
    }
    /**
     * 将byteBuffer中的数据写入socket
     * @param socket
     * @param byteBuffer
     * @throws IOException
     */
    public synchronized void write(Socket socket, ByteBuffer byteBuffer) throws IOException {
        byteBuffer.put(this.messageInProgress.sharedArray, this.messageInProgress.offset + this.bytesWritten, this.messageInProgress.length - this.bytesWritten);
        byteBuffer.flip();

        this.bytesWritten += socket.write(byteBuffer);
        byteBuffer.clear();

        if(bytesWritten >= this.messageInProgress.length){
            
            // 释放当前message占用的内存会内存池
            writeBuf.free(this.messageInProgress);
            
            if(this.writeQueue.size() > 0){
                
                this.messageInProgress = this.writeQueue.remove(0);
            } else {
                this.messageInProgress = null;
            }
        }
    }

    public boolean isEmpty() {
        return this.writeQueue.isEmpty() && this.messageInProgress == null;
    }
    
    public Message allocMessage() {
        return this.writeBuf.getMessage();
    }
}

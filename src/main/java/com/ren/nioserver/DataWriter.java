package com.ren.nioserver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.ren.util.ResizableArray;
import com.ren.util.ResizableArrayBuffer;

public class DataWriter {
    
    private List<ResizableArray> queue = new ArrayList<>();
    
    private ResizableArray processingMessage = null;
    
    private int bytesWritten = 0;
    /**
     * 一个消息过来, 把它放入内置的队列中
     * @param message
     */
    public void enqueue(ResizableArray message) {
        if (processingMessage == null) {
            processingMessage = message;
        } else {
            queue.add(message);
        }
    }
    /**
     * message -> byteBuf, byteBuf must has long size
     * 通过byteBuf写到channel, 努力去写, haha
     * 
     * 改进：假如 message 太长, byteBuf 装不下, 按照
     * 
     * @param socket
     * @param byteBuf
     */
    public void write(Socket socket, ByteBuffer byteBuf) {
        while (processingMessage != null) {
            int min = Math.min(byteBuf.remaining(), processingMessage.getLength() - bytesWritten);
            
            byteBuf.put(processingMessage.getBuf(), processingMessage.getOffset() + bytesWritten, min);
            byteBuf.flip();
            // 尽最大努力把byteBuf中的数据写入channel, 记录写了的数量
            this.bytesWritten += socket.write(byteBuf);
            byteBuf.clear();
            
            // 如果写完了.
            if(bytesWritten >= this.processingMessage.getLength()){
                if(this.queue.size() > 0){
                    this.processingMessage = this.queue.remove(0);
                } else {
                    this.processingMessage = null;
                    //todo unregister from selector
                }
                return;
            }
        }
    }
    /**
     * 当前是否有消息需要写?
     * @return
     */
    public boolean isEmpty(){
        return queue.isEmpty() && processingMessage == null;
    }
}

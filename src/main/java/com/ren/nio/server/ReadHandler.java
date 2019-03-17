package com.ren.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;

public class ReadHandler  {
    private Socket socket;
    
    private Selector writeSelector;
    
    private IMessageProcessor messageProcessor;
    public ReadHandler(Socket socket, Selector writeSelector, IMessageProcessor processor) {
        super();
        this.socket = socket;
        this.writeSelector = writeSelector;
        this.messageProcessor = processor;
    }
    public void process() {
        // 调用socket的reader读取数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
        try {
            socket.messageReader.read(socket, byteBuffer);
            // 处理所有请求
            Queue<Message> msgs = socket.messageReader.getMessages();
            if (!msgs.isEmpty()) {
                for (Iterator<Message> iterator = msgs.iterator(); iterator.hasNext();) {
                    Message msg = (Message) iterator.next();
                    iterator.remove();
                    try {
                        messageProcessor.process(msg, socket.messageWriter);
                    } catch (Exception e) {
                        e.printStackTrace(); // 或许应该自定义一些异常  
                    } finally {
                        // 回收这个已经处理完毕的消息占用的内存到内存池
                        socket.messageReader.free(msg);
                    }
                    
                }
                socket.socketChannel.register(writeSelector, SelectionKey.OP_WRITE,
                        socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //may be keep-alive
            
        }
    }

}

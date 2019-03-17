package com.jenkov.nioserver.http;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ren.nio.server.IMessageReader;
import com.ren.nio.server.Message;
import com.ren.nio.server.MessageBuffer;
import com.ren.nio.server.Socket;
import com.sun.javafx.image.impl.ByteRgb;


public class HttpMessageReader implements IMessageReader {

    private MessageBuffer messageBuffer    = null;

    private Queue<Message> completeMessages = new ConcurrentLinkedQueue<>();
    private Message       nextMessage      = null;

    public HttpMessageReader() {
    }

    @Override
    public void init(MessageBuffer readMessageBuffer) {
        this.messageBuffer        = readMessageBuffer;
        this.nextMessage          = messageBuffer.getMessage();
        this.nextMessage.metaData = new HttpHeaders();
    }

    @Override
    public synchronized void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int bytesRead = socket.read(byteBuffer);
        byteBuffer.flip();
        if(byteBuffer.remaining() == 0){
            byteBuffer.clear();
            return;
        }

        this.nextMessage.writeToMessage(byteBuffer);

        int endIndex = HttpUtil.parseHttpRequest(this.nextMessage.sharedArray, this.nextMessage.offset, this.nextMessage.offset + this.nextMessage.length, (HttpHeaders) this.nextMessage.metaData);
        if (endIndex != -1) {
            Message message = this.messageBuffer.getMessage();
            message.metaData = new HttpHeaders();
            // 将nextMessage中的 第一个请求数据之后的 所有请求数据放入当前message
            message.writePartialMessageToMessage(nextMessage, endIndex);
            
            completeMessages.add(nextMessage);
            nextMessage = message;
        }
        byteBuffer.clear();
    }


    @Override
    public Queue<Message> getMessages() {
        return this.completeMessages;
    }

    @Override
    public void free(Message message) {
        synchronized (messageBuffer) {
            messageBuffer.free(message);
        }
    }
    
}

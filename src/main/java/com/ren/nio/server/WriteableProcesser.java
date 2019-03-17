package com.ren.nio.server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class WriteableProcesser implements Runnable {
    private Selector writeSelector;
    private ExecutorService exec;
    private Queue<Socket> connectionQueue;
    public WriteableProcesser(Queue<Socket> connectionQueue, ExecutorService exec, Selector writeSelector) {
        super();
        this.writeSelector = writeSelector;
        this.exec = exec;
        this.connectionQueue = connectionQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int n = writeSelector.selectNow();
                if (n == 0) {
                    continue;
                }
                for (Iterator<SelectionKey> iterator = writeSelector.selectedKeys().iterator(); iterator.hasNext();) {
                    SelectionKey key =  iterator.next();
                    iterator.remove();
                    
                    Socket socket = (Socket) key.attachment();
                    socket.messageWriter.write(socket, ByteBuffer.allocate(4096));
                    // 取消监听写事件
                    key.cancel();
                    // read 0 , may be dont close ... 
                    // 如果不关闭, 浏览器的普通get请求可以 keep-alive, 
                    // 但是后台的ajax请求, 一直不会发下一次请求, 如果第一次请求没有断开???????
                    if (socket.willClose) {
                        key.cancel();
                        socket.socketChannel.close();
                        connectionQueue.remove(socket);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

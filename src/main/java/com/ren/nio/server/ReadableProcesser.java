package com.ren.nio.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * 处理读就绪事件线程
 * @author REN
 *
 */
public class ReadableProcesser implements Runnable {
    
    private Selector readSelector;
    private Selector writeSelector;
    private ExecutorService exec;
    private IMessageProcessor processor;
    private Queue<Socket> connQueue;
    public ReadableProcesser(Queue<Socket> connQueue, ExecutorService exec, Selector readSelector, Selector writeSelector, IMessageProcessor processor) {
        this.readSelector = readSelector;
        this.writeSelector = writeSelector;
        this.exec = exec;
        this.processor = processor;
        this.connQueue = connQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int n = readSelector.selectNow();
                if ( n == 0 ) {
                    continue;
                }
                for (Iterator<SelectionKey> iterator = readSelector.selectedKeys().iterator(); iterator.hasNext();) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    Socket socket = (Socket) key.attachment();
                    // 处理读就绪事件
                    System.out.println(socket.socketId + "读取就绪--> now handle it");
                    new ReadHandler(socket, writeSelector, processor).process();
                    
                    // 如果读到-1
                    if (socket.endOfStreamReached) {
                        // 立马关闭
                        key.cancel();
                        socket.socketChannel.close();
                        connQueue.remove(socket);
                    }
                    // 如果读到0
                    
                }
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

package com.ren.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.ren.util.ResizableArray;
import com.ren.util.ResizableArrayBuffer;

public class SocketProcessor implements Runnable {
    
    private final Queue<Socket> acceptQueue;
    private final Selector readSelector;
    private final Selector writeSelector;
    
    private final Map<Integer, Socket> socketMap;
    /**
     * 现在还没有使用 写缓冲, DataWrite 实现时, 是通过 将resizableArray 分批 , 通过 复制到 wbuf, 由 buf -> channel实现
     */
    private ResizableArrayBuffer writeBuffer; 
    private ResizableArrayBuffer readBuffer;
    private ByteBuffer wbuf = ByteBuffer.allocate(1024 * 1024);
    private ByteBuffer rbuf = ByteBuffer.allocate(1024 * 1024);
    
    private Queue<ResizableArray> outMessageQueue = new LinkedBlockingQueue<>();
    private WriteProxy writeProxy;
    private MessageProcessor messageProcessor;
    
    private Set<Socket> emptyMessageSocket = new HashSet<>();
    private Set<Socket> notEmptyMessageSocket = new HashSet<>();
    
    
    public SocketProcessor(Queue<Socket> acceptQueue, 
            ResizableArrayBuffer writeBuffer,
                ResizableArrayBuffer readBuffer) throws IOException {
        this.acceptQueue = acceptQueue;
        socketMap = new HashMap<>();
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.writeBuffer = writeBuffer;
        this.readBuffer = readBuffer;
        /**
         * 写代理有一个容器,维护了所有需要 写出的消息.
         */
        writeProxy = new WriteProxy(outMessageQueue, writeBuffer);
        messageProcessor = new MessageProcessor();
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                executeCycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void executeCycle() throws IOException {
        // poll socket from queue into map
        // read message from socket into 
        newSocket();
        readFromSockets();
        writeToSockets();
    }
    
    private void writeToSockets() throws IOException {
        
        takeNewOutMessage();
        
        // 清除所有的no data socket, and un regist
        cancelEmptySockets();
        
        // registe the socket channel that has data
        registNotEmptySockets();
        
        int n = writeSelector.selectNow();
        if (n > 0) {
            Set<SelectionKey> keySet = writeSelector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                
                writeToSocket(key);
                
                iterator.remove();
            }
        }
    }
    private void writeToSocket(SelectionKey key) {
        Socket socket = (Socket) key.attachment();
        DataWriter writer = socket.getWriter();
        writer.write(socket, wbuf);
        if (writer.isEmpty()) {
            this.emptyMessageSocket.add(socket);
        }
    }
    /**
     * take newRes msg into the socket-writer
     * then, socket-writer will write them into channel.
     */
    private void takeNewOutMessage() {
        ResizableArray res = outMessageQueue.poll();
        while (res != null) {
            Socket socket = socketMap.get(res.socket_id);
            if (socket != null) {
                DataWriter writer = socket.getWriter();
                if (writer.isEmpty()) {
                    emptyMessageSocket.remove(socket);
                    notEmptyMessageSocket.add(socket);
                    writer.enqueue(res);
                } else {
                    writer.enqueue(res);
                }
            }
            res = outMessageQueue.poll();
        }
    }
    private void cancelEmptySockets() {
        for (Socket s : emptyMessageSocket) {
            s.getSocketChannel().keyFor(writeSelector).cancel();
        }
        emptyMessageSocket.clear();
    }
    private void registNotEmptySockets() throws ClosedChannelException {
        for (Socket s : notEmptyMessageSocket) {
            s.getSocketChannel().register(writeSelector, SelectionKey.OP_WRITE, s);
        }
        notEmptyMessageSocket.clear();
    }
    private void newSocket() throws IOException {
        Socket socket = acceptQueue.poll();
        while (socket != null) {
            socketMap.put(socket.getId(), socket);
            // 非阻塞IO 开启
            socket.getSocketChannel().configureBlocking(false);
            // 注册到readSelector
            socket.getSocketChannel().register(readSelector, SelectionKey.OP_READ, socket);
            // 初始化write和
            socket.setReader(new DataReader(readBuffer));
            socket.setWriter(new DataWriter());
            socket = acceptQueue.poll();
        }
    }
    
    private void readFromSockets() throws IOException {
        int n = readSelector.selectNow();
        if (n > 0) {
            Set<SelectionKey> keySet = readSelector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                
                readFromSocket(key);
                iterator.remove();
            }
        }
    }

    private void readFromSocket(SelectionKey key) throws IOException {
        Socket socket = (Socket) key.attachment();
        socket.getReader().read(socket, rbuf);
        
        List<ResizableArray> fullMs = socket.getReader().completes();
        if (!fullMs.isEmpty()) {
            for (ResizableArray array : fullMs) {
                array.socket_id = socket.getId(); // 此处把 ID 赋值
                // 这里, 可以开启多个线程去处理.  或者, 用一个线程池
                messageProcessor.process(array, writeProxy);
            }
        }
        fullMs.clear();
        if (socket.isEndOfStreamReached()) {
            socketMap.remove(socket.getId());
            key.attach(null);
            key.cancel();
            key.channel().close();
        }
    }

}

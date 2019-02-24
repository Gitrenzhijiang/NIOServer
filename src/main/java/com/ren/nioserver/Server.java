package com.ren.nioserver;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.ren.util.ResizableArrayBuffer;

public class Server {
    
    public static void main(String[] args) {
        new Server().start();
    }
    
    private int tcp_port;
    
    
    private SocketAccepter socketAccept;
    private SocketProcessor socketProcessor;
    
    private ResizableArrayBuffer writeBuffer;
    private ResizableArrayBuffer readBuffer;
    
    public void start() {
        // we can config it in file
        this.tcp_port = 22222;
        Queue<Socket> acceptQueue = new ArrayBlockingQueue<>(1024);
        // 创建缓冲区
        readBuffer = new ResizableArrayBuffer();
        writeBuffer = new ResizableArrayBuffer();
        try {
            socketAccept = new SocketAccepter(acceptQueue, tcp_port);
            socketProcessor = new SocketProcessor(acceptQueue, writeBuffer, readBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(socketAccept).start();
        new Thread(socketProcessor).start();
    }
    
    
}

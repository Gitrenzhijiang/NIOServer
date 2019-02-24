package com.ren.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public class SocketAccepter implements Runnable {
    
    private final Queue<Socket> acceptQueue;
    private final int tcp_port;
    private ServerSocketChannel serverSocketChannel;
    
    public SocketAccepter(Queue<Socket> acceptQueue, int tcp_port) throws IOException {
        this.acceptQueue = acceptQueue;
        this.tcp_port = tcp_port;
    }
    
    @Override
    public void run() {
        
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(tcp_port));
            serverSocketChannel.configureBlocking(false);
            
            // 这里其实把accept的 socketChannel 放入队列.
            while (true) {
                
                SocketChannel socketChannel = serverSocketChannel.accept();
                
                if (socketChannel != null) {
                    acceptQueue.add(new Socket(Socket.INIT_ID++, socketChannel));
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

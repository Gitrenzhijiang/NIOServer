package com.ren.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;

public class SocketAccepter implements Runnable{

    private int tcpPort = 0;
    private ServerSocketChannel serverSocket = null;

    private Queue<Socket> socketQueue = null;
    
    private Selector acceptSelector = null;
    
    private Selector readSelector = null;
    
    private MessageBuffer readBuffer;
    private MessageBuffer writeBuffer;
    private IMessageReaderFactory readerFactory;
    public SocketAccepter(int tcpPort, Queue socketQueue, Selector readSelector, 
            MessageBuffer readBuffer, MessageBuffer writeBuffer, IMessageReaderFactory readerFactory)  {
        this.tcpPort     = tcpPort;
        this.socketQueue = socketQueue;
        this.readSelector = readSelector;
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
        this.readerFactory = readerFactory;
    }

    public void run() {
        try{
            this.acceptSelector = Selector.open();
            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.configureBlocking(false);
            this.serverSocket.bind(new InetSocketAddress(tcpPort));
            
            this.serverSocket.register(acceptSelector, SelectionKey.OP_ACCEPT);
        } catch(IOException e){
            e.printStackTrace();
            return;
        }


        while (!Thread.currentThread().isInterrupted()) {
            try{
                int n = acceptSelector.select(1000);
                if (n == 0) {
                    continue;
                }
                
                for (Iterator<SelectionKey> iterator = acceptSelector.selectedKeys().iterator(); iterator.hasNext();) {
                    SelectionKey key =  iterator.next();
                    iterator.remove();
                    SocketChannel socketChannel = this.serverSocket.accept();
                    if (socketChannel == null) {
                        continue;
                    }
                    
                    System.out.println("Socket accepted: " + socketChannel);

                    //todo check if the queue can even accept more sockets.
                    Socket socket = new Socket(socketChannel, readBuffer, writeBuffer, this.readerFactory);
                    socketChannel.configureBlocking(false);

                    this.socketQueue.add(socket);
                    // 注册读事件
                    System.out.println(System.currentTimeMillis());
                    socketChannel.register(readSelector, SelectionKey.OP_READ, socket);
                    
                    System.out.println(System.currentTimeMillis());
                    System.out.println("now:" + socketQueue.size());
                    socketQueue.stream().forEach((e)->{
                        System.out.println(e.socketId + "," + e.socketChannel + ":::--->" + e.socketChannel.isOpen());
                    });
                }
                

            } catch(IOException e){
                e.printStackTrace();
            }

        }

    }
}

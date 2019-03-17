package com.ren.bio.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    
    
    private int corePoolSize = 8;
    private int maximumPoolSize = 15;
    private long keepAliveTime = 10;
    private TimeUnit unit = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>(300);
    private ExecutorService exec = new ThreadPoolExecutor
            (corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    private ServerSocket serverSocket;
    
    private int port = 8000;

    public Server() throws IOException {
        serverSocket = new ServerSocket(port);
    }
    
    public void server() {
     // accept Thread
         AtomicInteger INIT_ID = new AtomicInteger(0);
        Runnable acceptTask = new Runnable() {
            
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("socket conn");
                        exec.execute(new SocketProcessor(INIT_ID.incrementAndGet(), exec, socket, new Handler()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        exec.execute(acceptTask);
        
    }
    public static void main(String[] args) throws IOException {
        new Server().server();
    }
    
}

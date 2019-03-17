package com.ren.bio.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketProcessor implements Runnable{
    private Socket socket;
    private boolean launch = false;
    
    private Handler handler;
    private ExecutorService exec;
    private int id;
    
    
    
    public SocketProcessor(int id, ExecutorService exec, Socket socket, Handler handler) {
        super();
        this.exec = exec;
        this.socket = socket;
        launch = true;
        this.handler = handler;
        
        this.id = id;
    }
    
    
    @Override
    public void run() {
        try {
            // 具体的处理逻辑如下
            // 可能抛出异常
            System.out.println("id = " + id);
            handler.process(socket);
            // 反射调用
//            for (Method m : socket.getClass().getMethods()) {
//                if (m.getName().startsWith("get")) {
//                    System.out.println(m.getName() + " 被调用: " + m.invoke(socket));
//                }
//            }
            if (!socket.isClosed()) {
                
//                socket.setKeepAlive(true);
                launch = true;
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            launch = false;
            System.out.println(id  + ": socket " + socket.isClosed());
        } finally {
            if (launch) {
                exec.execute(new SocketProcessor(id, exec, socket, handler));
            } else {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
}

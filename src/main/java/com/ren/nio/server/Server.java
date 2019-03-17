package com.ren.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jenkov.nioserver.http.HttpMessageReaderFactory;



public class Server {

    private SocketAccepter  socketAccepter  = null;
    private ReadableProcesser readableProcesser;
    private WriteableProcesser writeableprocesser;
    

    private int tcpPort = 0;
    private IMessageReaderFactory messageReaderFactory = null;
    private IMessageProcessor     messageProcessor = null;
    /**
     * 下面是线程池的字段.
     */
    private int corePoolSize = 5;
    private int maximumPoolSize = 20;
    private long keepAliveTime = 10;
    private TimeUnit unit = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(50);

    private ExecutorService exec = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    
    /**
     * Selectors 
     */
    private Selector readSelector = null;
    
    private Selector writeSelector = null;
    // Queue, 已经连接的socket 队列
    private Queue<Socket> connectionQueue = new ConcurrentLinkedQueue<>();
    
    public Server(int tcpPort, IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor) {
        this.tcpPort = tcpPort;
        this.messageReaderFactory = messageReaderFactory;
        this.messageProcessor = messageProcessor;
    }
    static String httpResponse = "HTTP/1.1 200 OK\r\n" + 
            "Content-Length: 31\r\n" + 
            "Content-Type: application/json\r\n" +
            "Access-Control-Allow-Origin: *\r\n" +
            "\r\n" + 
            "data:{\"name\":\"ren\", \"age\":21}\n\n";
    public static void main(String[] args) throws IOException {
        Server server = new Server(8000, new HttpMessageReaderFactory(), new IMessageProcessor() {
            
            @Override
            public void process(Message message, MessageWriter messageWriter) {
                // 打印message的内容
//                System.out.println(new String(message.sharedArray, message.offset, message.length));
                // 响应
                Message response = messageWriter.allocMessage();
                response.socketId = message.socketId;
                
                
                ByteBuffer buf = ByteBuffer.allocate(2048);
                buf.put(httpResponse.getBytes());
                buf.flip();
                response.writeToMessage(buf);
                
                messageWriter.enqueue(response);
            }
        });
        
        server.start();
    }
    
    public void start() throws IOException {
        
        if (messageProcessor == null) {
            throw new RuntimeException("应该给予一些处理消息并响应的对象");
        }
        readSelector = Selector.open();
        writeSelector = Selector.open();
        /**
         * 初始化内存池
         */
        MessageBuffer readBuffer  = new MessageBuffer();
        MessageBuffer writeBuffer = new MessageBuffer();
        
        socketAccepter = new SocketAccepter(tcpPort, connectionQueue, readSelector, 
                readBuffer, writeBuffer, messageReaderFactory);
        this.readableProcesser = new ReadableProcesser(connectionQueue, exec, readSelector, writeSelector, this.messageProcessor);
        this.writeableprocesser = new WriteableProcesser(connectionQueue, exec, writeSelector);
        exec.execute(socketAccepter);
        exec.execute(readableProcesser);
        exec.execute(writeableprocesser);
        
    }

    public IMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public void setMessageProcessor(IMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }


}

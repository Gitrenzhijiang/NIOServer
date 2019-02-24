package com.ren.nioserver;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.ren.util.ResizableArray;

public class MessageProcessor {
    static String httpResponse = "HTTP/1.1 200 OK\r\n" +
            "Content-Length: 38\r\n" +
            "Content-Type: text/html\r\n" +
            "\r\n" +
            "<html><body>Hello+World!</body></html>";
    /**
     * may be 在一些实现中, 可以解析request, 到 httpServletRequest 对象.
     * HttpServletResponse 对象, 底层亦可以由 ResizableArray 的实例 [缓冲数组]去实现.
     * @param request
     * @param writeProxy
     * @throws UnsupportedEncodingException
     */
    public void process(ResizableArray request, WriteProxy writeProxy) throws UnsupportedEncodingException {
        ResizableArray response = writeProxy.getArray();
        response.socket_id = request.socket_id;
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(httpResponse.getBytes());
        buf.flip();
        
        response.writeFromBuf(buf);
        
        writeProxy.enqueue(response);
        System.out.println("writeProxy queue size:" + writeProxy.queue.size());
    }
}

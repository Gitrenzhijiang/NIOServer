package com.ren.nioserver;
/**
 * a socket instance has a dataReader and a dataWrite instance.
 * dataReader handle the data input, and save them
 * @author REN
 *
 */

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jenkov.nioserver.Message;
import com.jenkov.nioserver.http.HttpHeaders;
import com.jenkov.nioserver.http.HttpUtil;
import com.ren.util.ResizableArray;
import com.ren.util.ResizableArrayBuffer;

public class DataReader {
    
    private ResizableArrayBuffer dataBuffer;
    
    private List<ResizableArray> completes;
    
    private ResizableArray next; // may be it is null or comtain part data 
    
    public DataReader(ResizableArrayBuffer dataBuffer) {
        super();
        this.dataBuffer = dataBuffer;
        this.completes = new ArrayList<>(24);
    }
    
    public void read(Socket socket, ByteBuffer byteBuf) {
        int rsize = socket.read(byteBuf);
        if (rsize == 0 || rsize == -1) {
            return;
        }
        if (next == null) {
            next = dataBuffer.getArray();
            next.metaData = new HttpHeaders();
        }
        // byteBuf -> next
        byteBuf.flip();
        if (-1 == next.writeFromBuf(byteBuf)) {
            throw new RuntimeException("-1");
        }
        byteBuf.clear();
        
        int endIndex = HttpUtil.parseHttpRequest(this.next.getBuf(), this.next.getOffset(), this.next.getOffset() + this.next.getLength(), (HttpHeaders) this.next.metaData);
        if(endIndex != -1) {
            ResizableArray message = this.dataBuffer.getArray();
            message.metaData = new HttpHeaders();
            
            message.writePartialMessageToMessage(next, endIndex);
            
            completes.add(next);
            next = message;
        }
    }
    
    public List<ResizableArray> completes(){
        return completes;
    }
}

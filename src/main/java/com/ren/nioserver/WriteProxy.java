package com.ren.nioserver;

import java.util.Queue;

import com.ren.util.ResizableArray;
import com.ren.util.ResizableArrayBuffer;

public class WriteProxy {
    Queue<ResizableArray> queue;
    
    ResizableArrayBuffer buffer;

    public WriteProxy(Queue<ResizableArray> queue, ResizableArrayBuffer buffer) {
        super();
        this.queue = queue;
        this.buffer = buffer;
    }
    
    public ResizableArray getArray() {
        return buffer.getArray();
    }
    
    public void enqueue(ResizableArray message) {
        queue.offer(message);
    }
}

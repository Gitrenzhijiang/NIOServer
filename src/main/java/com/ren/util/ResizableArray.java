package com.ren.util;

import java.nio.ByteBuffer;

public class ResizableArray {
    private byte[] buf;
    private int offset;
    private int capacity;
    private int length;
    
    private ResizableArrayBuffer resizableArrayBuf;
    
    public int socket_id;
    public Object metaData;
    
    public ResizableArray(ResizableArrayBuffer resizableArrayBuf) {
        super();
        this.resizableArrayBuf = resizableArrayBuf;
    }

    /* public method */
    public int writeFromBuf(ByteBuffer buf) {
        int remaining = buf.remaining();

        while(this.length + remaining > capacity){
            if(!this.resizableArrayBuf.expandArray(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(remaining, this.capacity - this.length);
        buf.get(this.buf, this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;

        return bytesToCopy;
    }
    
    public void setBuf(byte[] buf) {
        this.buf = buf;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
    public int getOffset() {
        return this.offset;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public byte[] getBuf() {
        return this.buf;
    }
    public int getCapacity() {
        return capacity;
    }
    
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    /**
     * 将next 中的[full message][part..] part部分放入this
     * @param next
     * @param endIndex
     */
    public void writePartialMessageToMessage(ResizableArray next, int endIndex) {
        int startIndexOfPartialMessage = next.offset + endIndex;
        int lengthOfPartialMessage     = (next.offset + next.length) - endIndex;

        System.arraycopy(next.buf, startIndexOfPartialMessage, this.buf, this.offset, lengthOfPartialMessage);
    }
}

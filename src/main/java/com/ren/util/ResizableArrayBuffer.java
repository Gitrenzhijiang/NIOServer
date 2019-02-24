package com.ren.util;

public class ResizableArrayBuffer {
    public static int KB = 1024;
    public static int MB = 1024 * KB;
    
    private int capacity = 0;
    private byte[] shardBytes;
    
    private int smallBlockSize = 0;
    private int smallBlockCount = 0;
    
    private int mediumBlockSize = 0;
    private int mediumBlockCount = 0;
    
    private int largeBlockSize = 0;
    private int largeBlockCount = 0;
    
    private QueueIntFlip smallFreeBlocks = null;
    private QueueIntFlip mediumFreeBlocks = null;
    private QueueIntFlip largeFreeBlocks = null;
    
    public ResizableArrayBuffer() {
        this(4 * KB, 1024, 128 * KB, 128, 1 * MB, 16);
    }
    
    public ResizableArrayBuffer(int smallBlockSize, int smallBlockCount, int mediumBlockSize, int mediumBlockCount,
            int largeBlockSize, int largeBlockCount) {
        super();
        this.smallBlockSize = smallBlockSize;
        this.smallBlockCount = smallBlockCount;
        this.mediumBlockSize = mediumBlockSize;
        this.mediumBlockCount = mediumBlockCount;
        this.largeBlockSize = largeBlockSize;
        this.largeBlockCount = largeBlockCount;
        this.capacity = smallBlockSize * smallBlockCount + 
                mediumBlockSize * mediumBlockCount + largeBlockSize * largeBlockCount;
        this.shardBytes = new byte[this.capacity];
        
        smallFreeBlocks = new QueueIntFlip(smallBlockCount);
        mediumFreeBlocks = new QueueIntFlip(mediumBlockCount);
        largeFreeBlocks = new QueueIntFlip(largeBlockCount);
        
        int smallStartIndex = 0; int smallEndIndex = smallBlockCount * smallBlockSize;
        for (;smallStartIndex < smallEndIndex;smallStartIndex+=smallBlockSize) {
            smallFreeBlocks.put(smallStartIndex);
        }
        
        int mediumStartIndex = smallEndIndex;
        int mediumEndIndex = mediumStartIndex + mediumBlockCount * mediumBlockSize;
        for (;mediumStartIndex < mediumEndIndex;mediumStartIndex += mediumBlockSize) {
            mediumFreeBlocks.put(mediumStartIndex);
        }
        
        int largeStartIndex = mediumEndIndex;
        int largeEndIndex = largeStartIndex + largeBlockCount * largeBlockSize;
        for (;largeStartIndex < largeEndIndex;largeStartIndex += largeBlockSize) {
            largeFreeBlocks.put(largeStartIndex);
        }
    }
    /**
     * 如果返回null, 表示当前无小数组可用
     * @return
     */
    public ResizableArray getArray() {
        // first, allocate a small buf
        int index = smallFreeBlocks.take();
        if (index == -1) {
            return null;
        }
        
        ResizableArray array = new ResizableArray(this);
        array.setBuf(this.shardBytes);
        array.setOffset(index);
        array.setCapacity(smallBlockSize);
        array.setLength(0);
        
        return array;
    }
    /**
     * 小数组->中数组, 中数组->大数组
     * 如果已经是largeArray, 不能在扩大了,返回false
     * @param resizableArray
     * @return
     */
    public boolean expandArray(ResizableArray resizableArray) {
        if (resizableArray.getCapacity() == smallBlockSize) {
            return moveArray(resizableArray, smallFreeBlocks, mediumFreeBlocks, mediumBlockSize);
        } else if (resizableArray.getCapacity() == mediumBlockSize) {
            return moveArray(resizableArray, mediumFreeBlocks, largeFreeBlocks, largeBlockSize);
        } else {
            return false;
        }
    }
    private boolean moveArray(ResizableArray resizableArray, QueueIntFlip sourceQIF, 
            QueueIntFlip destQIF, int newCapacity) {
        int pindex = destQIF.take();
        if (pindex == -1) {
            return false;
        }
        System.arraycopy(this.shardBytes, resizableArray.getOffset(), this.shardBytes, pindex, resizableArray.getLength());        
        // free source arr
        sourceQIF.put(resizableArray.getOffset());
        
        resizableArray.setBuf(this.shardBytes);
        resizableArray.setCapacity(newCapacity);
        resizableArray.setOffset(pindex);
        return true;
    }
    
    public void free(ResizableArray resizableArray) {
        if (resizableArray.getCapacity() == smallBlockSize) {
            smallFreeBlocks.put(resizableArray.getOffset());
        } else if (resizableArray.getCapacity() == mediumBlockSize) {
            mediumFreeBlocks.put(resizableArray.getOffset());
        } else if (resizableArray.getCapacity() == largeBlockSize) {
            largeFreeBlocks.put(resizableArray.getOffset());
        }
    }
}

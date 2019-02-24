package com.ren.util;

public class QueueIntFlip {
    
    private int[] elements = null;
    
    private int capacity = 0;
    private int writePos = 0;
    private int readPos = 0;
    private boolean flipped = false;
    
    public QueueIntFlip(int capacity) {
        this.capacity = capacity;
        this.elements = new int[capacity];
    }
    
    public void reset() {
        this.writePos = 0;
        this.readPos = 0;
        this.flipped = false;
    }
    
    public int available() {
        if (!flipped) { 
            return writePos - readPos;
        }
        // 翻转之后
        return readPos - writePos;
    }
    public int remainingCapacity() {
        if (!flipped) {
            return capacity - writePos;
        }
        return readPos - writePos;
    }
    public boolean put(int element) {
        if (!flipped) {
            if (writePos == capacity) {
                writePos = 0; // 放满了
                flipped = true; // 翻转
                
                if (writePos < readPos) {
                    // 这时还可以放
                    elements[writePos++] = element;
                    return true;
                } else {
                    return false;
                }
            } else {
                // 容量还有
                elements[writePos++] = element;
                return true;
            }
        } else {
            // 已经翻转了
            if (writePos < readPos) {
                elements[writePos++] = element;
                return true;
            } else {
                // 满了
                return false;
            }
        }
    }
    public int put(int[] newElements, int length) {
        int newElementsReadPos = 0;
        if (!flipped) {
            int min = Math.min(length, capacity - writePos);
            for (;newElementsReadPos < min;newElementsReadPos++) {
                elements[writePos++] = newElements[newElementsReadPos];
            }
            // may be already put over
            if (newElementsReadPos == length) {
                return newElementsReadPos;
            } else {
                // 不能放下, 
                flipped = true;
                writePos = 0;
                min = Math.min(length - newElementsReadPos, readPos);
                for (;this.writePos < min; writePos++) {
                    elements[writePos] = newElements[newElementsReadPos++];
                }
                return newElementsReadPos;
            }
        } else {
            // 翻转之后,
            int min = Math.min(writePos + length, readPos);
            for (;writePos < min;writePos++) {
                elements[writePos] = newElements[newElementsReadPos++];
            }
            return newElementsReadPos;
        }
    }
    
    /**
     * 如果是-1, 数组中无数据可拿出
     * @return
     */
    public int take() {
        if (!flipped) {
            if (readPos < writePos) {
                return elements[readPos++];
            } else {
                return -1;
            }
        } else {
            if (readPos < capacity) {
                return elements[readPos++];
            } else {
                // readPos == capacity
                readPos = 0;
                flipped = false;
                if (readPos < writePos) {
                    return elements[readPos++]; 
                } else {
                    return -1;
                }
            }
        }
    }
    /**
     * 从队列翻转数组中拿出元素到info数组, 直到源数组中没有元素或目标数组已满
     * @param into
     * @param length
     * @return 拿出的元素个数
     */
    public int take(int[] into, int length) {
        int takeNum = 0;
        if (!flipped) {
            int min = Math.min(length, writePos - readPos);
            for (;takeNum < min;) {
                into[takeNum++] = elements[readPos++];
            }
            return takeNum;
        } else {
            int min = Math.min(length, capacity - readPos);
            for (;takeNum < min;) {
                into[takeNum++] = elements[readPos++];
            }
            if (takeNum == length) {
                return takeNum;
            } else {
                // 此时还需要读取前面的内容
                flipped = false;
                readPos = 0;
                min = Math.min(writePos, length - takeNum);
                for (;readPos < min;readPos++) {
                    into[takeNum++] = this.elements[this.readPos];
                }
                return takeNum;
            }
        }
    }
}

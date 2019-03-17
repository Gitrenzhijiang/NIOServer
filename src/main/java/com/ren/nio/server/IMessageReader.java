package com.ren.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public interface IMessageReader {

    public void init(MessageBuffer readMessageBuffer);

    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException;

    public Queue<Message> getMessages();

    public void free(Message message);
}

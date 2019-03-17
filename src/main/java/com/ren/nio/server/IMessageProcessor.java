package com.ren.nio.server;

public interface IMessageProcessor {

    public void process(Message message, MessageWriter messageWriter);

}

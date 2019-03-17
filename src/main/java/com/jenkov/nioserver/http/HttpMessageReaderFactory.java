package com.jenkov.nioserver.http;

import com.ren.nio.server.IMessageReader;
import com.ren.nio.server.IMessageReaderFactory;

/**
 * Created by jjenkov on 18-10-2015.
 */
public class HttpMessageReaderFactory implements IMessageReaderFactory {

    public HttpMessageReaderFactory() {
    }

    @Override
    public IMessageReader createMessageReader() {
        return new HttpMessageReader();
    }
}

package com.vovamisjul.dserver.tcp;

import java.nio.channels.AsynchronousSocketChannel;

public class TcpConnection {

    private final AsynchronousSocketChannel channel;

    public TcpConnection(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }
}

package com.vovamisjul.dserver.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {

    private static Logger LOG = LogManager.getLogger(ConnectionHandler.class);

    private final AsynchronousServerSocketChannel listener;

    @Autowired
    private ConnectionManager connectionManager;

    public ConnectionHandler(AsynchronousServerSocketChannel listener) {
        this.listener = listener;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Void att) {
        listener.accept(null, this);
        connectionManager.addNewConnection(new TcpConnection(channel));
    }

    @Override
    public void failed(Throwable throwable, Void att) {

    }
}

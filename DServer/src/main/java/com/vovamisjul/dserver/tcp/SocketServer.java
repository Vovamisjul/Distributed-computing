package com.vovamisjul.dserver.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class SocketServer implements Runnable {

    private static Logger LOG = LogManager.getLogger(SocketServer.class);

    @Value("tcp.host")
    private String host;

    @Value("tcp.port")
    private int port;

    @Override
    public void run() {
        try {
            final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(5000));

            listener.accept(null, new ConnectionHandler(listener));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.vovamisjul.dserver.tcp;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ConnectionManager {

    private List<TcpConnection> connections = new LinkedList<>();

    public void addNewConnection(TcpConnection connection) {
        connections.add(connection);
    }
}

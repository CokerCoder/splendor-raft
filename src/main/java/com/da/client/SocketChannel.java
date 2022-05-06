package com.da.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * Implement IO to servers
 */
public class SocketChannel {

    private final String host;
    private final int port;

    public SocketChannel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Object send(Object payload) {
        try (Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(this.host, this.port));
            this.write(socket.getOutputStream(), payload);
            return this.read(socket.getInputStream());
        } catch (IOException e) {
            throw new ChannelException("failed to send and receive", e);
        }
    }

    // TODO Object -> byte[]
    private int write(OutputStream os, Object payload) throws IOException {
//        os.write();
        return 0;
    }

    // TODO byte[] -> Object
    private Object read(InputStream is) throws IOException {
        byte[] bytes = is.readAllBytes();
        return null;
    }
}


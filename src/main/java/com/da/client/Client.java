package com.da.client;

/**
 * The client that communicates to kv-store server
 */
public class Client {

    private final ServerRouter serverRouter;

    public Client(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

    public void set(String key, byte[] value) {
        serverRouter.send(new SetCommand(key, value));
    }

    public byte[] get(String key) {
        return (byte[]) serverRouter.send(new GetCommand(key));
    }

    public ServerRouter getServerRouter() {
        return serverRouter;
    }

}

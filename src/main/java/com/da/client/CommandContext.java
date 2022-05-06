package com.da.client;

import com.da.node.NodeId;
import com.da.node.nodestatic.Address;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class CommandContext {
    private Client client;
    private boolean running = false; // TODO remove???

    public CommandContext(Map<NodeId, Address> serverMap) {
        this.client = new Client(buildServerRouter(serverMap));
    }

    public void get(String[] args) {
        String key = "";
        for (int i = 1; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                key = args[i];
                break;
            }
        }
        byte[] result = client.get(key);
        System.out.println(Arrays.toString(result));
    }

    public void set(String[] args) {
        String key = "", value = "";
        for (int i = 1; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                if (key.isEmpty()) {
                    key = args[i];
                }
                else {
                    value = args[i];
                    break;
                }
            }
        }
        client.set(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private static ServerRouter buildServerRouter(Map<NodeId, Address> serverMap) {
        ServerRouter router = new ServerRouter();
        for (NodeId nodeId : serverMap.keySet()) {
            Address address = serverMap.get(nodeId);
            router.add(nodeId, new SocketChannel(address.getHost(), address.getPort()));
        }
        return router;
    }
}

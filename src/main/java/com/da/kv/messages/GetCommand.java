package com.da.kv.messages;

public class GetCommand {
    private final String key;

    public GetCommand(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

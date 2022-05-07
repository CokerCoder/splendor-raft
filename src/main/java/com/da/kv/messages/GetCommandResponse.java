package com.da.kv.messages;

public class GetCommandResponse {
    private final byte[] value;

    public GetCommandResponse(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }
}

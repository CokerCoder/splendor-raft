package com.da.kv.messages;

public class Failure {
    private final int code;
    private final String message;

    public Failure(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

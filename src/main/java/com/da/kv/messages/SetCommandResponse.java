package com.da.kv.messages;

public class SetCommandResponse {

    private final boolean isSuccess;
    public boolean isSuccess() {
        return isSuccess;
    }

    private final String leaderId; // if need to redirect
    public String getLeaderId() {
        return leaderId;
    }

    private final int errorCode;
    private final String errorMessage;
    public int getErrorCode() {
        return errorCode;
    }
    public String getErrorMessage() {
        return errorMessage;
    }

    public SetCommandResponse() {
        this.isSuccess = true;
        this.leaderId = null;
        errorCode = -1;
        errorMessage = null;
    }

    public SetCommandResponse(int errorCode, String errorString) {
        this.isSuccess = false;
        this.leaderId = null;
        this.errorCode = errorCode;
        this.errorMessage = errorString;
    }

    public SetCommandResponse(String leaderId) {
        this.isSuccess = false;
        this.errorCode = -1;
        this.errorMessage = null;
        this.leaderId = leaderId;
    }
    
}

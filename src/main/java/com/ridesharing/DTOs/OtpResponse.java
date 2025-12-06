package com.ridesharing.DTOs;

import lombok.Data;

@Data
public class OtpResponse {
    private boolean success;
    private String message;

    public OtpResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

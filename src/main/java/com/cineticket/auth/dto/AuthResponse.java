package com.cineticket.auth.dto;

public class AuthResponse {
    private String token;
    private String message;

    public AuthResponse(String result) {
        this.message = result;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

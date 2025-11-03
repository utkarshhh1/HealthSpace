package com.healthspace.backend.dto;

// This class holds the JWT token we send to the client
public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
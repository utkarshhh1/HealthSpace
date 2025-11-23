package com.healthspace.backend.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private Long userId;

    // NEW FIELDS
    private String name;          // patient name OR doctor name
    private Boolean isVerified;   // doctor verification flag

    public AuthResponse(String token, String email, String role, Long userId) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    // FULL constructor for convenience
    public AuthResponse(String token, String email, String role, Long userId,
                        String name, Boolean isVerified) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.isVerified = isVerified;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
}

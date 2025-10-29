package com.example.spring_project_mid.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token; // The JWT
    private String username;
    private String email;
    private String message;
}
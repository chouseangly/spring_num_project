package com.example.spring_project_mid.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
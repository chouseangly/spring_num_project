package com.example.spring_project_mid.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotEmpty
    private String token;

    @NotEmpty
    @Size(min = 8, max = 50)
    private String newPassword;
}
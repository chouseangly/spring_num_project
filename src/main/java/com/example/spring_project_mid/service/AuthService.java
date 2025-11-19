package com.example.spring_project_mid.service;

import com.example.spring_project_mid.dto.*;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.enums.Role;
import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registers a new user and sends an OTP to their email for verification.
     */
    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }

        String otp = generateOtp();
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .enabled(false)
                .verificationOtp(otp)
                .otpExpiryTime(LocalDateTime.now().plusMinutes(10))
                .build();
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    /**
     * Verifies the OTP provided by the user.
     */
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getVerificationOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        user.setEnabled(true);
        user.setVerificationOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .message("Account verified successfully.")
                .build();
    }

    /**
     * Authenticates a user and returns a JWT token upon successful login.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please check your email for OTP.");
        }

        String jwtToken = jwtService.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .message("Login successful.")
                .build();
    }

    /**
     * Initiates the forgot password process by generating a reset token and sending it via email.
     */
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiryTime(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String resetLink = "http://your-frontend-url/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    /**
     * Resets the user's password using the provided reset token.
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiryTime(null);
        userRepository.save(user);
    }

    /**
     * Generates a 6-digit OTP.
     */
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
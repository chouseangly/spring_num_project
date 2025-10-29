package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    // Used for login
    Optional<User> findByUsernameOrEmail(String username, String email);
    // Used for OTP verification
    Optional<User> findByVerificationOtp(String otp);
    // Used for password reset
    Optional<User> findByResetPasswordToken(String token);
}
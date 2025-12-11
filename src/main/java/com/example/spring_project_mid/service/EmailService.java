package com.example.spring_project_mid.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an OTP email to the specified recipient.
     *
     * @param to  Recipient's email address
     * @param otp One-time password to be sent
     */
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your Account Verification OTP");
        message.setText("Your OTP is: " + otp + "\nIt is valid for 1 minutes.");
        mailSender.send(message);
    }

    /**
     * Sends a password reset email to the specified recipient.
     *
     * @param to        Recipient's email address
     * @param resetLink Password reset link
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below (valid for 1 hour):\n" + resetLink);
        mailSender.send(message);
    }
}
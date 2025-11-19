package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Notification;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    /**
     * Displays the notifications page for the authenticated user.
     */
    @GetMapping("/notifications")
    public String showNotifications(@AuthenticationPrincipal User user, Model model) {

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        model.addAttribute("notifications", notifications);

        return "notifications";
    }
}
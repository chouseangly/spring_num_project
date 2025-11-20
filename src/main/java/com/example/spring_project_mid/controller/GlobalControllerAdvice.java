package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Notification;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationRepository notificationRepository;

    /**
     * Fetches the current user's notifications and adds them to the model
     * for every request.
     */
    @ModelAttribute("userNotifications")
    public List<Notification> addUserNotifications(@AuthenticationPrincipal User user) {
        if (user != null) {
            return notificationRepository.findByUserOrderByCreatedAtDesc(user);
        }
        return Collections.emptyList();
    }

    /**
     * Calculates and adds the count of *unread* notifications to the model.
     */
    @ModelAttribute("unreadNotificationCount")
    public long addUnreadNotificationCount(@ModelAttribute("userNotifications") List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return 0;
        }
        return notifications.stream().filter(n -> !n.isRead()).count();
    }

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Notification;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * This class adds global attributes to the model for all controllers.
 * This is used to make data (like notifications) available to common
 * fragments, such as the navbar.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationRepository notificationRepository;

    /**
     * Fetches the current user's notifications and adds them to the model
     * for every request.
     *
     * @param user  The currently authenticated user (can be null if not logged in).
     * @param model The Spring model to which attributes are added.
     */
    @ModelAttribute("userNotifications")
    public List<Notification> addUserNotifications(@AuthenticationPrincipal User user) {
        if (user != null) {
            // Fetch the user's notifications
            // We use the same query as your NotificationController
            return notificationRepository.findByUserOrderByCreatedAtDesc(user);
        }
        // Return an empty list if no user is logged in
        return Collections.emptyList();
    }

    /**
     * Calculates and adds the count of *unread* notifications to the model.
     *
     * @param notifications This list is injected by Spring from the
     * addUserNotifications method above.
     * @return The count of unread notifications.
     */
    @ModelAttribute("unreadNotificationCount")
    public long addUnreadNotificationCount(@ModelAttribute("userNotifications") List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return 0;
        }
        // Count how many notifications have isRead = false
        return notifications.stream().filter(n -> !n.isRead()).count();
    }
}
package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Notification;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // <-- Add this import

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

    /**
     * Notification as read and redirects to its target link.
     */
    @GetMapping("/notifications/{id}/read")
    public String markAsReadAndRedirect(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
             return "redirect:/notifications";
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }

        String link = notification.getLink();
        return "redirect:" + (link != null && !link.isEmpty() ? link : "/");
    }

    /**
     * Marks all notifications as read for the current user.
     */
    @PostMapping("/notifications/mark-all-read")
    public String markAllAsRead(@AuthenticationPrincipal User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        
        // Loop through and set all to read
        notifications.forEach(n -> n.setRead(true));
        
        // Save all changes at once
        notificationRepository.saveAll(notifications);
        
        return "redirect:/notifications";
    }
}
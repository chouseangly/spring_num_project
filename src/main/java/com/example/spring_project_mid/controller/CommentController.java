package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.*;
import com.example.spring_project_mid.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;

    // --- EDIT COMMENT ---
    @PostMapping("/{id}/edit")
    public String editComment(
            @PathVariable Long id,
            @RequestParam("content") String content,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Security: Check if current user is the owner
        if (!comment.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only edit your own comments.");
            return "redirect:/posts/" + comment.getPost().getId();
        }

        comment.setContent(content);
        commentRepository.save(comment);

        // Notify the person involved (Post Owner or Parent Commenter)
        sendNotification(comment, user, "edited");

        return "redirect:/posts/" + comment.getPost().getId();
    }

    // --- DELETE COMMENT ---
    @PostMapping("/{id}/delete")
    public String deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only delete your own comments.");
            return "redirect:/posts/" + comment.getPost().getId();
        }

        Long postId = comment.getPost().getId();

        // Notify before deletion (so we still have the user details)
        sendNotification(comment, user, "deleted");

        commentRepository.delete(comment);

        return "redirect:/posts/" + postId;
    }

    // --- HELPER: Send Notification ---
    private void sendNotification(Comment comment, User actor, String action) {
        User targetUser;
        String context; // "reply" or "comment"

        // Determine who to notify
        if (comment.getParentComment() != null) {
            targetUser = comment.getParentComment().getUser(); // Notify parent commenter
            context = "their reply to your comment";
        } else {
            targetUser = comment.getPost().getUser(); // Notify post owner
            context = "their comment on your post";
        }

        // Don't notify if the user is interacting with themselves
        if (!targetUser.getId().equals(actor.getId())) {
            String msg = actor.getUsername() + " " + action + " " + context + ".";
            String link = "/posts/" + comment.getPost().getId();

            notificationRepository.save(Notification.builder()
                    .user(targetUser)
                    .message(msg)
                    .isRead(false)
                    .link(link)
                    .build());
        }
    }

    // --- SUSPEND/UNSUSPEND COMMENT (ADMIN ONLY) ---
    @PostMapping("/{id}/toggle-suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String toggleSuspend(@PathVariable Long id, HttpServletRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean newStatus = !comment.isSuspended();
        comment.setSuspended(newStatus);
        commentRepository.save(comment);

        String postLink = "/posts/" + comment.getPost().getId();

        if (newStatus) {
            // CASE: Suspending
            // 1. Notify the Comment Owner
            notificationRepository.save(Notification.builder()
                    .user(comment.getUser())
                    .message("Your comment on '" + comment.getPost().getTitle() + "' has been suspended by a moderator for violating community guidelines.")
                    .isRead(false)
                    .link(postLink)
                    .build());

            // 2. Notify the Post Owner (if they are not the comment owner)
            if (!comment.getPost().getUser().getId().equals(comment.getUser().getId())) {
                notificationRepository.save(Notification.builder()
                        .user(comment.getPost().getUser())
                        .message("A comment by " + comment.getUser().getUsername() + " on your post was suspended by a moderator.")
                        .isRead(false)
                        .link(postLink)
                        .build());
            }
        } else {
            // CASE: Unsuspending (Optional: Notify comment owner they are back)
            notificationRepository.save(Notification.builder()
                    .user(comment.getUser())
                    .message("Your comment on '" + comment.getPost().getTitle() + "' has been unsuspended.")
                    .isRead(false)
                    .link(postLink)
                    .build());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : postLink);
    }
}
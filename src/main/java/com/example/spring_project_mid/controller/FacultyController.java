package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.enums.Role;
import com.example.spring_project_mid.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/faculty")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FACULTY_ADMIN')") // Only Faculty Admins can access
public class FacultyController {

    private final PostRepository postRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        // 1. Check if admin has a faculty assigned
        if (user.getFaculty() == null) {
            model.addAttribute("error", "You are an admin, but you are not assigned to any faculty yet.");
            return "faculty/error"; // You can create a simple error page or reuse dashboard
        }

        // 2. Get posts for this faculty
        var posts = postRepository.findAllByFacultyOrderByCreatedAtDesc(user.getFaculty());

        model.addAttribute("faculty", user.getFaculty());
        model.addAttribute("posts", posts);
        return "faculty/dashboard";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Security Check: Ensure the post belongs to the admin's faculty
        if (post.getFaculty() != null && post.getFaculty().getId().equals(user.getFaculty().getId())) {
            postRepository.delete(post);
        }

        return "redirect:/faculty/dashboard";
    }
}
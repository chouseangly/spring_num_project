package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Faculty;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequestMapping("/faculty")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FACULTY_ADMIN')")
public class FacultyController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User principal, Model model) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Faculty faculty = user.getFaculty();

        if (faculty == null) {
            model.addAttribute("error", "You are an admin, but you are not assigned to any faculty yet.");
            model.addAttribute("posts", Collections.emptyList());
            return "faculty/dashboard";
        }

        var posts = postRepository.findAllByFacultyOrderByCreatedAtDesc(faculty);

        model.addAttribute("faculty", faculty);
        model.addAttribute("posts", posts);
        return "faculty/dashboard";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        try {
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            if (user.getFaculty() != null && post.getFaculty() != null &&
                    post.getFaculty().getId().equals(user.getFaculty().getId())) {
                postRepository.delete(post);
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return "redirect:/faculty/dashboard";
    }
}
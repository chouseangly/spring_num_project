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
import java.util.List;

@Controller
@RequestMapping("/faculty")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FACULTY_ADMIN')")
public class FacultyController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Displays the faculty admin dashboard with posts related to the faculty.
     * Supports searching within the faculty.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User principal,
                            @RequestParam(value = "q", required = false) String query,
                            Model model) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Faculty faculty = user.getFaculty();

        if (faculty == null) {
            model.addAttribute("error", "You are an admin, but you are not assigned to any faculty yet.");
            model.addAttribute("posts", Collections.emptyList());
            return "faculty/dashboard";
        }

        List<Post> posts;
        if (query != null && !query.trim().isEmpty()) {
            posts = postRepository.searchPostsInFaculty(query.trim(), faculty);
        } else {
            posts = postRepository.findAllByFacultyOrderByCreatedAtDesc(faculty);
        }

        model.addAttribute("faculty", faculty);
        model.addAttribute("posts", posts);
        model.addAttribute("searchQuery", query); // Pass query back to view
        return "faculty/dashboard";
    }

    /**
     * Deletes a post if it belongs to the faculty of the authenticated user.
     */
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        try {
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            // STRICT CHECK: Ensure the post belongs to the same faculty as the admin
            if (user.getFaculty() != null && post.getFaculty() != null &&
                    post.getFaculty().getId().equals(user.getFaculty().getId())) {
                postRepository.delete(post);
            }
        } catch (Exception e) {
            // Log error if needed
        }
        return "redirect:/faculty/dashboard";
    }
}
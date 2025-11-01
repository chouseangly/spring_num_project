package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Post; // Assuming you renamed Event to Post
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.PostRepository; // Assuming you renamed EventRepository to PostRepository
import com.example.spring_project_mid.service.PinataService; // We will create this
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PinataService pinataService; // Inject the new service

    @GetMapping("/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create-post"; // This will render templates/create-post.html
    }

    @PostMapping("/create")
    public String createPost(
            @ModelAttribute Post post,
            @AuthenticationPrincipal User user
            // @RequestParam(value = "postImage", required = false) MultipartFile postImage // For traditional form submit
    ) {
        post.setUser(user);
        // Set other defaults if necessary, e.g., createdAt
        postRepository.save(post);
        return "redirect:/"; // Redirect to homepage or post detail
    }

    // --- NEW: Endpoint for AJAX Pinata Upload ---
    @PostMapping("/upload-to-pinata")
    @ResponseBody // Important for returning JSON directly
    public ResponseEntity<?> uploadFileToPinata(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "No file uploaded"));
        }
        try {
            String ipfsHash = pinataService.uploadFile(file);
            String gatewayUrl = pinataService.getGatewayUrl(ipfsHash); // Get the public gateway URL
            return ResponseEntity.ok(Map.of("ipfsHash", ipfsHash, "gatewayUrl", gatewayUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Error uploading to Pinata: " + e.getMessage()));
        }
    }
}
package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Image; // <-- IMPORT
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.service.PinataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashSet; // <-- IMPORT
import java.util.Map;
import java.util.Set; // <-- IMPORT

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

    // --- START: MODIFIED createPost METHOD ---
    @PostMapping("/create")
    public String createPost(
            @ModelAttribute Post post,
            @RequestParam(value = "mediaUrls", required = false) String mediaUrls, // <-- Get URLs as separate param
            @AuthenticationPrincipal User user
    ) {
        post.setUser(user);

        // --- NEW LOGIC ---
        // If mediaUrls string is not empty, split it, create Image objects, and add to post
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            String[] urls = mediaUrls.split(",");
            Set<Image> images = new HashSet<>();
            for (String url : urls) {
                if (!url.trim().isEmpty()) { // Avoid empty strings
                    Image newImage = new Image();
                    newImage.setUrl(url.trim());
                    newImage.setPost(post); // Link image to post
                    images.add(newImage);
                }
            }
            post.setImages(images); // Add the set of images to the post
        }
        // --- END NEW LOGIC ---

        postRepository.save(post); // This will save the post and cascade-save the new Image objects
        return "redirect:/"; // Redirect to homepage or post detail
    }
    // --- END: MODIFIED createPost METHOD ---


    // --- NEW: Endpoint for AJAX Pinata Upload (No Change) ---
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
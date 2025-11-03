package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Image;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.service.PinataService; // <-- IMPORT THE NEW SERVICE
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // <-- IMPORT ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <-- IMPORT MultipartFile

import java.util.Map; // <-- IMPORT Map
import java.util.Set;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PinataService pinataService; // <-- INJECT THE NEW SERVICE

    @GetMapping("/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create-post"; // This will render templates/create-post.html
    }

    // --- START: MODIFIED createPost METHOD (Your original code, which is correct) ---
    // In: chouseangly/spring_num_project/spring_num_project-main/src/main/java/com/example/spring_project_mid/controller/PostController.java

    @PostMapping("/create")
    public String createPost(
            @ModelAttribute Post post,
            @RequestParam(value = "mediaUrls", required = false) String mediaUrls, // <-- Get URLs as separate param
            @AuthenticationPrincipal User user
    ) {
        post.setUser(user);

        // --- START OF FIX ---

        // Get the set that's already on the post object (which is an empty HashSet)
        Set<Image> images = post.getImages();

        // If mediaUrls string is not empty, split it, create Image objects, and add to post
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            String[] urls = mediaUrls.split(",");

            // Note: We are no longer creating a new HashSet here.

            for (String url : urls) {
                if (!url.trim().isEmpty()) { // Avoid empty strings
                    Image newImage = new Image();
                    newImage.setUrl(url.trim());

                    // CRITICAL: Link the Image back to the Post
                    newImage.setPost(post);

                    // Add the new image to the *original* set from the post
                    images.add(newImage);
                }
            }
            // We DON'T need post.setImages(images); anymore because
            // we modified the 'images' set we got from the post directly.
        }
        // --- END OF FIX ---

        // This save will now cascade and save the Image objects
        // because they are in the 'images' set and have their 'post' field set.
        postRepository.save(post);
        return "redirect:/"; // Redirect to homepage or post detail
    }
    // --- END: MODIFIED createPost METHOD ---


    // --- NEW: Endpoint for AJAX Pinata Upload ---
    /**
     * Handles the asynchronous file upload from create-post.html.
     * This endpoint is called by the JavaScript 'uploadFileToPinata' function.
     * It receives a file, uploads it to Pinata via PinataService,
     * and returns a JSON object with the { "gatewayUrl": "..." }.
     */
    @PostMapping("/upload-to-pinata")
    @ResponseBody // <-- IMPORTANT: Tells Spring to return JSON, not a template name
    public ResponseEntity<?> uploadToPinata(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            // 1. Call the service to upload the file
            String gatewayUrl = pinataService.uploadFileToPinata(file);

            // 2. Return the gatewayUrl in the JSON format the JavaScript expects
            return ResponseEntity.ok(Map.of("gatewayUrl", gatewayUrl));

        } catch (Exception e) {
            // 3. Return an error message if anything goes wrong
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
    // --- END: NEW Endpoint ---
}
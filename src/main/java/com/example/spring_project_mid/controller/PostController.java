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
    private final PinataService pinataService;

    @GetMapping("/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create-post";
    }

    /**
     * Handles the form submission for creating a new post.
     */
    @PostMapping("/create")
    public String createPost(
            @ModelAttribute Post post,
            @RequestParam(value = "mediaUrls", required = false) String mediaUrls,
            @AuthenticationPrincipal User user
    ) {
        post.setUser(user);

        Set<Image> images = post.getImages();

        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            String[] urls = mediaUrls.split(",");

            for (String url : urls) {
                if (!url.trim().isEmpty()) {
                    Image newImage = new Image();
                    newImage.setUrl(url.trim());

                    newImage.setPost(post);

                    images.add(newImage);
                }
            }
        }
        postRepository.save(post);
        return "redirect:/";
    }

    /**
     * Handles the asynchronous file upload from create-post.html.
     */
    @PostMapping("/upload-to-pinata")
    @ResponseBody
    public ResponseEntity<?> uploadToPinata(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            String gatewayUrl = pinataService.uploadFileToPinata(file);

            return ResponseEntity.ok(Map.of("gatewayUrl", gatewayUrl));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
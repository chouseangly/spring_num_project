package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Image;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.Vote; // <-- ADD IMPORT
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.VoteRepository; // <-- ADD IMPORT
import com.example.spring_project_mid.service.PinataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional; // <-- ADD IMPORT
import java.util.Set;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PinataService pinataService;
    private final VoteRepository voteRepository;

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
     * Toggles the like status for a post by the authenticated user.
     */
    @PostMapping("/{postId}/like")
    public String toggleLike(@PathVariable Long postId, @AuthenticationPrincipal User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Vote> existingVote = voteRepository.findByUserAndPost(user, post);

        if (existingVote.isPresent()) {
            voteRepository.delete(existingVote.get());
        } else {
            Vote newVote = Vote.builder()
                    .post(post)
                    .user(user)
                    .voteType(1)
                    .build();
            voteRepository.save(newVote);
        }

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
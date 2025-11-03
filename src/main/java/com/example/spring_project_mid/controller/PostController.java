package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Image;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;

    @GetMapping("/create")
    public String showCreatePostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create-post"; // This will render templates/create-post.html
    }

    // --- START: MODIFIED createPost METHOD ---
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


    // --- NEW: Endpoint for AJAX Pinata Upload (No Change) ---

}
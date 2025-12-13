package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.*;
import com.example.spring_project_mid.repository.*;
import com.example.spring_project_mid.service.PinataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PinataService pinataService;
    private final VoteRepository voteRepository;
    private final SavedPostRepository savedPostRepository;
    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Shows the form for creating a new post.
     */
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

        // --- ADD THIS LINE ---
        // Automatically assign the post to the user's faculty
        post.setFaculty(user.getFaculty());
        // --------------------

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
     * Shows the edit form for a specific post.
     */
    @GetMapping("/{id}/edit")
    public String showEditPostForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            return "redirect:/";
        }

        model.addAttribute("post", post);
        return "edit-post";
    }

    /**
     * Handles the form submission for updating a post.
     */
    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable Long id,
            @ModelAttribute Post postRequest,
            @RequestParam(value = "mediaUrls", required = false) String mediaUrls,
            @AuthenticationPrincipal User user
    ) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!existingPost.getUser().getId().equals(user.getId())) {
            return "redirect:/";
        }

        existingPost.setTitle(postRequest.getTitle());
        existingPost.setContent(postRequest.getContent());
        existingPost.setLinkUrl(postRequest.getLinkUrl());

        Set<String> newUrlSet = new HashSet<>();
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            Collections.addAll(newUrlSet, mediaUrls.split(","));
        }

        existingPost.getImages().removeIf(img -> !newUrlSet.contains(img.getUrl()));

        Set<String> currentUrlSet = existingPost.getImages().stream()
                .map(Image::getUrl)
                .collect(Collectors.toSet());

        for (String url : newUrlSet) {
            if (!currentUrlSet.contains(url) && !url.trim().isEmpty()) {
                Image img = new Image();
                img.setUrl(url.trim());
                img.setPost(existingPost);
                existingPost.getImages().add(img);
            }
        }

        postRepository.save(existingPost);
        return "redirect:/profile";
    }

    /**
     * Deletes a specific post.
     */
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getUser().getId().equals(user.getId())) {
            postRepository.delete(post);
        }

        return "redirect:/profile";
    }

    /**
     * Toggles the Like status for a post.
     */
    @PostMapping("/{postId}/like")
    public String toggleLike(@PathVariable Long postId, @AuthenticationPrincipal User user, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Vote> existingVote = voteRepository.findByUserAndPost(user, post);

        if (existingVote.isPresent()) {
            voteRepository.delete(existingVote.get());
        } else {
            Vote newVote = Vote.builder()
                    .post(post)
                    .user(user)
                    .voteType(1) // 1 for Like
                    .build();
            voteRepository.save(newVote);

            // --- Notification Logic: Post Liked ---
            // Only notify if the liker is NOT the post owner
            if (!post.getUser().getId().equals(user.getId())) {
                String msg = user.getUsername() + " liked your post: " + post.getTitle();
                String link = "/posts/" + postId;
                
                notificationRepository.save(Notification.builder()
                        .user(post.getUser())
                        .message(msg)
                        .isRead(false)
                        .link(link)
                        .build());
            }
        }

        // Redirect back to the previous page (to stay on feed or details page)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Toggles the Saved status for a post.
     * Prevent users from saving their own posts.
     */
    @PostMapping("/{postId}/save")
    public String toggleSave(@PathVariable Long postId, @AuthenticationPrincipal User user, jakarta.servlet.http.HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getUser().getId().equals(user.getId())) {
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/");
        }

        Optional<SavedPost> existingSave = savedPostRepository.findByUserAndPost(user, post);

        if (existingSave.isPresent()) {
            savedPostRepository.delete(existingSave.get());
        } else {
            SavedPost savedPost = SavedPost.builder()
                    .post(post)
                    .user(user)
                    .build();
            savedPostRepository.save(savedPost);
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
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

    /**
     * Toggles the suspended status of a post.
     * Only accessible by SUPER_ADMIN.
     */
    @PostMapping("/{id}/toggle-suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String toggleSuspend(@PathVariable Long id, HttpServletRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setSuspended(!post.isSuspended());
        postRepository.save(post);

        // Redirect back to the previous page (the profile page)
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Display Post Details with Comments
     */
    @GetMapping("/{id}")
    public String viewPostDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Filter for root comments (no parent) and sort them
        List<Comment> rootComments = post.getComments().stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

        model.addAttribute("post", post);
        model.addAttribute("comments", rootComments);
        return "post-details";
    }

    /**
     * Handle Comment Submission with Notification Logic
     */
    @PostMapping("/{postId}/comments")
    public String addComment(
            @PathVariable Long postId,
            @RequestParam("content") String content,
            @RequestParam(value = "parentCommentId", required = false) Long parentCommentId,
            @AuthenticationPrincipal User user
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();

        Comment parent = null;
        if (parentCommentId != null) {
            parent = commentRepository.findById(parentCommentId)
                    .orElse(null);
            comment.setParentComment(parent);
        }

        commentRepository.save(comment);

        // --- Notification Logic ---
        // Added logic to handle reply and post owner notifications
        
        // 1. Notify Parent Commenter (if this is a reply)
        if (parent != null) {
            User parentAuthor = parent.getUser();
            // Don't notify if user is replying to themselves
            if (!parentAuthor.getId().equals(user.getId())) {
                String msg = user.getUsername() + " replied to your comment on: " + post.getTitle();
                notificationRepository.save(Notification.builder()
                        .user(parentAuthor)
                        .message(msg)
                        .isRead(false)
                        .build());
            }
        }

        // 2. Notify Post Owner
        // We notify the post owner if:
        //  - They are not the one commenting.
        //  - AND they weren't just notified as the parent commenter (to avoid duplicate notifications for the same event).
        
        User postOwner = post.getUser();
        boolean isOwnerCommenting = postOwner.getId().equals(user.getId());
        boolean alreadyNotifiedAsParent = (parent != null && parent.getUser().getId().equals(postOwner.getId()));
        String postLink = "/posts/" + postId;

        if (!isOwnerCommenting && !alreadyNotifiedAsParent) {
            String msg = user.getUsername() + " commented on your post: " + post.getTitle();
            notificationRepository.save(Notification.builder()
                    .user(postOwner)
                    .message(msg)
                    .isRead(false)
                    .link(postLink)
                    .build());
        }

        return "redirect:/posts/" + postId;
    }
}
package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.dto.AuthResponse;
import com.example.spring_project_mid.dto.RegisterRequest;
import com.example.spring_project_mid.dto.VerifyOtpRequest;
import com.example.spring_project_mid.model.Comment;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.CommentRepository;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.model.SavedPost;
import com.example.spring_project_mid.repository.SavedPostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import com.example.spring_project_mid.service.AuthService;
import com.example.spring_project_mid.service.PinataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PinataService pinataService;
    private final SavedPostRepository savedPostRepository;

    /**
     * Shows the registration form.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "form/register";
    }

    /**
     * Processes the registration form submission.
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            bindingResult.addError(
                    new FieldError("registerRequest", "confirmPassword", "Passwords do not match")
            );
        }

        if (bindingResult.hasErrors()) {
            return "form/register";
        }

        try {
            authService.register(registerRequest);
        } catch (RuntimeException e) {
            bindingResult.addError(
                    new ObjectError("registerRequest", e.getMessage())
            );
            return "form/register";
        }

        redirectAttributes.addAttribute("email", registerRequest.getEmail());
        redirectAttributes.addFlashAttribute("infoMessage",
                "Registration initiated! Please check your email for the OTP.");
        return "redirect:/verify-otp";
    }

    /**
     * Shows the OTP verification form.
     */
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam("email") String email, Model model) {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail(email);

        // Calculate remaining time for the countdown
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getOtpExpiryTime() != null) {
                long secondsRemaining = java.time.Duration.between(
                    java.time.LocalDateTime.now(), 
                    user.getOtpExpiryTime()
                ).getSeconds();
                model.addAttribute("secondsRemaining", Math.max(0, secondsRemaining));
            }
        });

        model.addAttribute("verifyOtpRequest", verifyOtpRequest);
        model.addAttribute("email", email);
        return "form/verify-otp";
    }

    /**
     * Handles the resend OTP link.
     */
    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.resendOtp(email);
            redirectAttributes.addFlashAttribute("infoMessage", "A new OTP has been sent to your email.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        redirectAttributes.addAttribute("email", email);
        return "redirect:/verify-otp";
    }

    /**
     * Processes the OTP verification form submission.
     */
    @PostMapping("/verify-otp")
    public String processOtpVerification(
            @Valid @ModelAttribute("verifyOtpRequest") VerifyOtpRequest verifyOtpRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("email", verifyOtpRequest.getEmail());
            return "form/verify-otp";
        }

        try {
            AuthResponse response = authService.verifyOtp(verifyOtpRequest);
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            return "redirect:/login";

        } catch (RuntimeException e) {
            bindingResult.addError(new ObjectError("verifyOtpRequest", e.getMessage()));
            model.addAttribute("email", verifyOtpRequest.getEmail());
            return "form/verify-otp";
        }
    }

    /**
     * Shows the login form.
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("successMessage", model.getAttribute("successMessage"));
        return "form/login";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        // Owner sees ALL their posts (including suspended ones)
        List<Post> posts = postRepository.findAllByUserOrderByCreatedAtDesc(user);
        List<Comment> comments = commentRepository.findByUserOrderByCreatedAtDesc(user);
        List<SavedPost> savedPosts = savedPostRepository.findByUserOrderByCreatedAtDesc(user);

        List<Object> activities = new ArrayList<>();
        activities.addAll(posts);
        activities.addAll(comments);

        activities.sort((o1, o2) -> {
            LocalDateTime t1 = (o1 instanceof Post) ? ((Post) o1).getCreatedAt() : ((Comment) o1).getCreatedAt();
            LocalDateTime t2 = (o2 instanceof Post) ? ((Post) o2).getCreatedAt() : ((Comment) o2).getCreatedAt();
            return t2.compareTo(t1);
        });

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("comments", comments);
        model.addAttribute("activities", activities);
        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("isOwner", true); // Flag to show Edit/Delete buttons in HTML

        return "profile";
    }

    /**
     * Displays the home page with a list of posts.
     */
    /**
     * Displays the home page with a list of posts.
     */
    @GetMapping("/")
    public String showHomePage(Model model) {
        // CHANGED: Use findAllByOrderByCreatedAtDesc() to show ALL posts (including suspended)
        // Previous: List<Post> posts = postRepository.findAllBySuspendedFalseOrderByCreatedAtDesc();
        List<Post> posts = postRepository.findAllBySuspendedFalseOrderByCreatedAtDesc();

        model.addAttribute("posts", posts);
        return "home";
    }

    /**
     * ADDED: Handles Search Requests
     */
    @GetMapping("/search")
    public String searchPosts(@RequestParam(value = "q", required = false) String query, Model model) {
        List<Post> posts;
        if (query != null && !query.trim().isEmpty()) {
            // Note: This repository method still filters by suspended=false inside the @Query annotation
            // If you want search to ALSO show suspended posts, you must update PostRepository.java as well.
            posts = postRepository.searchPosts(query.trim());
        } else {
            // CHANGED: Match the homepage behavior
            posts = postRepository.findAllBySuspendedFalseOrderByCreatedAtDesc();
        }
        model.addAttribute("posts", posts);
        model.addAttribute("searchQuery", query);
        return "home";
    }
    /**
     * Shows the form for editing the user's profile.
     */
    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "form/edit-profile";
    }

    /**
     * Handles the submission of the profile update form.
     */
    @PostMapping("/profile/update")
    public String handleProfileUpdate(
            @RequestParam("displayName") String displayName,
            @RequestParam("bio") String bio,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        try {
            user.setDisplayName(displayName);
            user.setBio(bio);

            if (file != null && !file.isEmpty()) {
                String avatarUrl = pinataService.uploadFileToPinata(file);
                user.setAvatarUrl(avatarUrl);
            }

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * Displays another user's profile page.
     */
    @GetMapping("/users/{username}")
    public String showUserProfile(@PathVariable String username, Model model) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        boolean isOwner = targetUser.getUsername().equals(currentUsername);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        List<Post> posts;

        // LOGIC:
        // 1. If Owner or Admin: Show ALL posts (Active + Suspended).
        // 2. If Student/Faculty (Visitor): Show ONLY Active (Non-Suspended) posts.
        if (isOwner || isAdmin) {
            posts = postRepository.findAllByUserOrderByCreatedAtDesc(targetUser);
        } else {
            // This ensures students see the user's posts, provided they aren't suspended.
            posts = postRepository.findAllByUserAndSuspendedFalseOrderByCreatedAtDesc(targetUser);
        }

        List<Comment> comments = commentRepository.findByUserOrderByCreatedAtDesc(targetUser);

        // Only show saved posts if the viewer is the owner
        List<SavedPost> savedPosts;
        if (isOwner) {
            savedPosts = savedPostRepository.findByUserOrderByCreatedAtDesc(targetUser);
        } else {
            savedPosts = new ArrayList<>();
        }

        List<Object> activities = new ArrayList<>();
        // Only add posts to activity if they are allowed to be seen
        activities.addAll(posts);
        activities.addAll(comments);

        activities.sort((o1, o2) -> {
            LocalDateTime t1 = (o1 instanceof Post) ? ((Post) o1).getCreatedAt() : ((Comment) o1).getCreatedAt();
            LocalDateTime t2 = (o2 instanceof Post) ? ((Post) o2).getCreatedAt() : ((Comment) o2).getCreatedAt();
            return t2.compareTo(t1);
        });

        model.addAttribute("user", targetUser);
        model.addAttribute("posts", posts);
        model.addAttribute("activities", activities);
        model.addAttribute("comments", comments);
        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("isOwner", isOwner); // HTML uses this to hide Edit/Delete buttons

        return "profile";
    }
}
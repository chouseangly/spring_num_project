package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.dto.*;
import com.example.spring_project_mid.model.Comment;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.SavedPost;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.CommentRepository;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.SavedPostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import com.example.spring_project_mid.service.AuthService;
import com.example.spring_project_mid.service.PinataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "form/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            bindingResult.addError(new FieldError("registerRequest", "confirmPassword", "Passwords do not match"));
        }
        if (bindingResult.hasErrors()) {
            return "form/register";
        }
        try {
            authService.register(registerRequest);
        } catch (RuntimeException e) {
            bindingResult.addError(new ObjectError("registerRequest", e.getMessage()));
            return "form/register";
        }
        redirectAttributes.addAttribute("email", registerRequest.getEmail());
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam("email") String email, Model model) {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail(email);
        model.addAttribute("verifyOtpRequest", verifyOtpRequest);
        model.addAttribute("email", email);
        return "form/verify-otp";
    }

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
            authService.verifyOtp(verifyOtpRequest);
            return "redirect:/login";
        } catch (RuntimeException e) {
            bindingResult.addError(new ObjectError("verifyOtpRequest", e.getMessage()));
            model.addAttribute("email", verifyOtpRequest.getEmail());
            return "form/verify-otp";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "form/login";
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("posts", posts);
        return "home";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("posts", postRepository.findAllByUserOrderByCreatedAtDesc(user));
        model.addAttribute("isOwner", true);

        // FIX: Add empty activities list to prevent template crash
        model.addAttribute("activities", new ArrayList<>());

        return "profile";
    }

    /**
     * Fixed: Added mapping to handle profile viewing for other users.
     * This prevents the 404 error when clicking usernames in the post feed.
     */
    @GetMapping("/users/{username}")
    public String showUserProfile(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = currentUsername.equals(username);

        model.addAttribute("user", user);
        model.addAttribute("posts", postRepository.findAllByUserOrderByCreatedAtDesc(user));
        model.addAttribute("isOwner", isOwner);

        // FIX: Add empty activities list to prevent template crash
        model.addAttribute("activities", new ArrayList<>());

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "form/edit-profile"; // Points to src/main/resources/templates/form/edit-profile.html
    }

    @PostMapping("/profile/edit")
    public String handleEditProfile(@ModelAttribute User updatedUser,
                                    @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update fields
            currentUser.setDisplayName(updatedUser.getDisplayName());
            currentUser.setBio(updatedUser.getBio());

            // Handle Avatar Upload if a new file is provided
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = pinataService.uploadFileToPinata(avatarFile);
                currentUser.setAvatarUrl(avatarUrl);
            }

            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("emailRequest", new ForgotPasswordRequest());
        return "form/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@ModelAttribute ForgotPasswordRequest request, RedirectAttributes redirectAttributes) {
        try {
            authService.sendOtp(request.getEmail());
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/verify-otp-forgot";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/verify-otp-forgot")
    public String showVerifyOtpForgot(@RequestParam("email") String email, Model model) {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        model.addAttribute("verifyOtpRequest", request);
        return "form/verify-otp-forgot";
    }

    @PostMapping("/verify-forgot-otp")
    public String handleVerifyForgotOtp(@ModelAttribute VerifyOtpRequest request, RedirectAttributes redirectAttributes) {
        try {
            authService.verifyOtp(request);
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/reset-new-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/verify-otp-forgot";
        }
    }

    @GetMapping("/reset-new-password")
    public String showResetPasswordForm(@RequestParam("email") String email, Model model) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        model.addAttribute("resetPasswordRequest", request);
        return "form/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@ModelAttribute ResetPasswordRequest request, RedirectAttributes redirectAttributes) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/reset-new-password";
        }
        try {
            authService.updatePassword(request.getEmail(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("email", request.getEmail());
            return "redirect:/reset-new-password";
        }
    }
}
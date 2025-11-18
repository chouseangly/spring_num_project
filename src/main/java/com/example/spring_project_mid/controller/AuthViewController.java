package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.dto.AuthResponse;
import com.example.spring_project_mid.dto.RegisterRequest;
import com.example.spring_project_mid.dto.VerifyOtpRequest;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import com.example.spring_project_mid.service.AuthService;
import com.example.spring_project_mid.service.PinataService; // <-- IMPORT PinataService
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- IMPORT
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile; // <-- IMPORT
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PinataService pinataService;

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

        model.addAttribute("verifyOtpRequest", verifyOtpRequest);
        model.addAttribute("email", email);
        model.addAttribute("infoMessage", model.getAttribute("infoMessage"));
        return "form/verify-otp";
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

    /**
     * Displays the authenticated user's profile page.
     */
    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        List<Post> posts = postRepository.findAllByUserOrderByCreatedAtDesc(user);

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);

        return "profile";
    }

    /**
     * Displays the home page with a list of posts.
     */
    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("posts", posts);
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
}
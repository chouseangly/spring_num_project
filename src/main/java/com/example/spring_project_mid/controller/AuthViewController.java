package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.dto.AuthResponse; // <-- Import needed
import com.example.spring_project_mid.dto.RegisterRequest;
import com.example.spring_project_mid.dto.VerifyOtpRequest; // <-- Import needed
import com.example.spring_project_mid.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // <-- Import needed
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;

    // --- Registration Methods (Keep Existing) ---
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

        // --- CHANGE: Redirect to OTP page instead of login ---
        // Pass the email as a parameter to the OTP page
        redirectAttributes.addAttribute("email", registerRequest.getEmail());
        redirectAttributes.addFlashAttribute("infoMessage",
                "Registration initiated! Please check your email for the OTP.");
        return "redirect:/verify-otp"; // <-- Redirect to the OTP verification page
    }

    // --- START: New OTP Verification Methods ---

    /**
     * Shows the OTP verification form.
     * Expects an 'email' parameter in the URL (from the redirect after registration).
     */
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam("email") String email, Model model) {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail(email); // Pre-populate email

        model.addAttribute("verifyOtpRequest", verifyOtpRequest);
        model.addAttribute("email", email); // Also add email directly for display
        model.addAttribute("infoMessage", model.getAttribute("infoMessage")); // Show message from registration redirect
        return "form/verify-otp"; // Return the name of the new HTML file
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
            // If validation fails (e.g., OTP field is empty), show the form again
            model.addAttribute("email", verifyOtpRequest.getEmail()); // Keep email displayed
            return "form/verify-otp";
        }

        try {
            // Call the existing service method
            AuthResponse response = authService.verifyOtp(verifyOtpRequest);

            // On success, redirect to login with a success message
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            // Optionally: You could automatically log the user in here and redirect elsewhere
            return "redirect:/login";

        } catch (RuntimeException e) {
            // On failure (Invalid OTP, Expired OTP, User not found), add a global error
            bindingResult.addError(new ObjectError("verifyOtpRequest", e.getMessage()));
            model.addAttribute("email", verifyOtpRequest.getEmail()); // Keep email displayed
            return "form/verify-otp"; // Show the form again with the error
        }
    }
    // --- END: New OTP Verification Methods ---


    // --- Login Method (Keep Existing) ---
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("successMessage", model.getAttribute("successMessage"));
        // model.addAttribute("loginRequest", new LoginRequest()); // This is not needed for formLogin
        return "form/login";
    }

    // --- START: CHANGED THIS METHOD TO BE THE ROOT ---
    /**
     * Shows the home page. This is now the root URL.
     */
    @GetMapping("/")
    public String showHomePage() {
        // This tells Thymeleaf to render the "home.html" template
        return "home";
    }
    // --- END: CHANGED METHOD ---


    // --- Root Redirect (REMOVED) ---
    // @GetMapping("/")
    // public String redirectToLogin() {
    //    return "redirect:/login";
    // }
}
package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.dto.AuthResponse;
import com.example.spring_project_mid.dto.RegisterRequest;
import com.example.spring_project_mid.dto.VerifyOtpRequest;
import com.example.spring_project_mid.model.Event; // <-- IMPORT Event
import com.example.spring_project_mid.repository.EventRepository; // <-- IMPORT EventRepository
import com.example.spring_project_mid.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List; // <-- IMPORT List

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final EventRepository eventRepository; // <-- ADD EventRepository

    // --- Registration Methods (No Change) ---
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

        redirectAttributes.addAttribute("email", registerRequest.getEmail());
        redirectAttributes.addFlashAttribute("infoMessage",
                "Registration initiated! Please check your email for the OTP.");
        return "redirect:/verify-otp";
    }

    // --- OTP Verification Methods (No Change) ---
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam("email") String email, Model model) {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail(email);

        model.addAttribute("verifyOtpRequest", verifyOtpRequest);
        model.addAttribute("email", email);
        model.addAttribute("infoMessage", model.getAttribute("infoMessage"));
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
            AuthResponse response = authService.verifyOtp(verifyOtpRequest);
            redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            return "redirect:/login";

        } catch (RuntimeException e) {
            bindingResult.addError(new ObjectError("verifyOtpRequest", e.getMessage()));
            model.addAttribute("email", verifyOtpRequest.getEmail());
            return "form/verify-otp";
        }
    }

    // --- Login Method (No Change) ---
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("successMessage", model.getAttribute("successMessage"));
        return "form/login";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        // Get username from Spring Security's context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // You could also fetch the full user from the repository if you need more details
        // For now, we'll just pass the username to the page
        model.addAttribute("username", username);

        return "profile"; // This will render templates/profile.html
    }

    // --- START: MODIFIED HOME PAGE METHOD ---
    /**
     * Shows the home page, now with a feed of events.
     */
    @GetMapping("/")
    public String showHomePage(Model model) { // <-- ADD Model

        // Fetch all events, newest first
        List<Event> events = eventRepository.findAllByOrderByCreatedAtDesc();

        // Add the list to the model so Thymeleaf can access it
        model.addAttribute("events", events);

        // This tells Thymeleaf to render the "home.html" template
        return "home";
    }
    // --- END: MODIFIED METHOD ---
}
package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')") // Secure this whole controller
public class AdminController {

    private final UserRepository userRepository;

    // 1. List all users
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    // 2. Suspend/Activate User (Toggle Enabled status)
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent disabling yourself (Super Admin safety)
        // You might want to add a check here using @AuthenticationPrincipal

        user.setEnabled(!user.isEnabled()); // Toggle true/false
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    // 3. Delete User (Optional)
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return "redirect:/admin/users";
    }
}
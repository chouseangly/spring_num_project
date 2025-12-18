package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.enums.Role;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * Displays the admin dashboard with statistics.
     */
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("postCount", postRepository.count());
        return "admin/dashboard";
    }

    /**
     * Lists users with Search, Filter, and Sort functionality.
     */
    @GetMapping("/users")
    public String listUsers(Model model,
                            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
                            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "role", required = false) Role role,
                            @RequestParam(value = "status", required = false) Boolean status) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        List<User> users = userRepository.searchUsers(keyword, role, status, sort);

        model.addAttribute("users", users);

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        
        model.addAttribute("allRoles", Role.values());

        return "admin/users";
    }

    /**
     * Toggles the enabled status of a user.
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    /**
     * Deletes a user by ID.
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    /**
     * Displays the user edit form.
     */
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("allRoles", Role.values()); // For dropdown
        return "admin/user-edit";
    }

    /**
     * Updates user details such as role and faculty.
     */
    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam("role") Role role) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(role);

        userRepository.save(user);
        return "redirect:/admin/users";
    }
}
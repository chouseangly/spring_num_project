package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Faculty;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.enums.Role;
import com.example.spring_project_mid.repository.FacultyRepository;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    private final FacultyRepository facultyRepository;

    // --- 1. DASHBOARD ---
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("postCount", postRepository.count());
        model.addAttribute("facultyCount", facultyRepository.count());
        // You can add latest 5 users or posts here if you want
        return "admin/dashboard";
    }

    // --- 2. USER MANAGEMENT (Existing) ---
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    // --- 3. FACULTY MANAGEMENT ---
    @GetMapping("/faculties")
    public String listFaculties(Model model) {
        model.addAttribute("faculties", facultyRepository.findAll());
        model.addAttribute("newFaculty", new Faculty()); // For the create form
        return "admin/faculties";
    }

    @PostMapping("/faculties/create")
    public String createFaculty(@ModelAttribute Faculty faculty) {
        facultyRepository.save(faculty);
        return "redirect:/admin/faculties";
    }

    @PostMapping("/faculties/{id}/delete")
    public String deleteFaculty(@PathVariable Long id) {
        facultyRepository.deleteById(id);
        return "redirect:/admin/faculties";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("allFaculties", facultyRepository.findAll()); // For dropdown
        model.addAttribute("allRoles", Role.values()); // For dropdown
        return "admin/user-edit";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam("role") Role role,
                             @RequestParam(value = "facultyId", required = false) Long facultyId) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Update Role
        user.setRole(role);

        // 2. Update Faculty
        if (facultyId != null) {
            Faculty faculty = facultyRepository.findById(facultyId)
                    .orElse(null);
            user.setFaculty(faculty);
        } else {
            user.setFaculty(null); // Clear faculty if none selected
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }
}
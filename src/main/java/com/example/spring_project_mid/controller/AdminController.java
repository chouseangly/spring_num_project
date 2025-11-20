package com.example.spring_project_mid.controller;

import com.example.spring_project_mid.model.Faculty;
import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.enums.Role;
import com.example.spring_project_mid.repository.FacultyRepository;
import com.example.spring_project_mid.repository.PostRepository;
import com.example.spring_project_mid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    private final FacultyRepository facultyRepository;

    /**
     * Displays the admin dashboard with statistics.
     */
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("postCount", postRepository.count());
        model.addAttribute("facultyCount", facultyRepository.count());
        return "admin/dashboard";
    }

    /**
     * Lists all users for admin management.
     */
    @GetMapping("/users")
    public String listUsers(Model model,
                            @RequestParam(value = "sortField", defaultValue = "id") String sortField,
                            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        model.addAttribute("users", userRepository.findByRoleNot(Role.SUPER_ADMIN, sort));

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

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
     * Lists all faculties for admin management.
     */
    @GetMapping("/faculties")
    public String listFaculties(Model model) {
        model.addAttribute("faculties", facultyRepository.findAll());
        model.addAttribute("newFaculty", new Faculty()); // For the create form
        return "admin/faculties";
    }

    /**
     * Creates a new faculty.
     */
    @PostMapping("/faculties/create")
    public String createFaculty(@ModelAttribute Faculty faculty) {
        facultyRepository.save(faculty);
        return "redirect:/admin/faculties";
    }

    /**
     * Deletes a faculty by ID.
     */
    @PostMapping("/faculties/{id}/delete")
    public String deleteFaculty(@PathVariable Long id) {
        facultyRepository.deleteById(id);
        return "redirect:/admin/faculties";
    }

    /**
     * Displays the user edit form.
     */
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("allFaculties", facultyRepository.findAll()); // For dropdown
        model.addAttribute("allRoles", Role.values()); // For dropdown
        return "admin/user-edit";
    }

    /**
     * Updates user details such as role and faculty.
     */
    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam("role") Role role,
                             @RequestParam(value = "facultyId", required = false) Long facultyId) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(role);

        if (facultyId != null) {
            Faculty faculty = facultyRepository.findById(facultyId)
                    .orElse(null);
            user.setFaculty(faculty);
        } else {
            user.setFaculty(null);
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }
}
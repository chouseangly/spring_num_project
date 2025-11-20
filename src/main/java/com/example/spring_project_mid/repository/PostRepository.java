package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Faculty; // Import
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

    // --- ADD THIS NEW METHOD ---
    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    List<Post> findAllByFacultyOrderByCreatedAtDesc(Faculty faculty);
}
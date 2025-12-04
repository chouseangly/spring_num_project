package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Faculty;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    List<Post> findAllByFacultyOrderByCreatedAtDesc(Faculty faculty);

    // --- Global Search (For Home Page) ---
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@Param("query") String query);

    // --- Faculty-Specific Search (For Faculty Dashboard) ---
    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    @Query("SELECT p FROM Post p WHERE p.faculty = :faculty AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY p.createdAt DESC")
    List<Post> searchPostsInFaculty(@Param("query") String query, @Param("faculty") Faculty faculty);
}
package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Faculty;
import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // --- FOR OWNER / ADMIN (Shows everything) ---
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

    // --- FOR PUBLIC VIEW (Hides suspended posts) ---
    
    // 1. For Homepage
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllBySuspendedFalseOrderByCreatedAtDesc();

    // 2. For Visiting other profiles
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserAndSuspendedFalseOrderByCreatedAtDesc(User user);

    // 3. Updated Search (Only searches non-suspended posts)
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.suspended = false ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@Param("query") String query);
    
    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    List<Post> findAllByFacultyOrderByCreatedAtDesc(Faculty faculty);

    @Override
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "comments.user", "images", "savedPosts"})
    Optional<Post> findById(Long id);
}
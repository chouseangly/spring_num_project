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
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

    // --- FOR PUBLIC VIEW (Hides suspended posts) ---

    // 1. For Homepage

    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByOrderByCreatedAtDesc();
    // 2. For Visiting other profiles
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserAndSuspendedFalseOrderByCreatedAtDesc(User user);

    // 3. Updated Search (Only searches non-suspended posts)
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.user.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.user.displayName) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") AND p.suspended = false " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@Param("query") String query);

    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    List<Post> findAllByFacultyOrderByCreatedAtDesc(Faculty faculty);

    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    @Query("SELECT p FROM Post p WHERE p.faculty = :faculty AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY p.createdAt DESC")
    List<Post> searchPostsInFaculty(@Param("query") String query, @Param("faculty") Faculty faculty);
}
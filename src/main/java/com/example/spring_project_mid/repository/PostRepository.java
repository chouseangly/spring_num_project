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

    // --- FOR PUBLIC VIEW ---

    // 1. For Homepage (Shows ALL posts)
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByOrderByCreatedAtDesc();

    // 2. For Visiting other profiles (Legacy method, usually replaced by findAllByUser above)
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserAndSuspendedFalseOrderByCreatedAtDesc(User user);

    // 3. Updated Search (Matches 1 or more characters anywhere in Title, Content, or Name)
    // REMOVED "AND p.suspended = false" so it shows everything.
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.user.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.user.displayName) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@Param("query") String query);

    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    List<Post> findAllByFacultyOrderByCreatedAtDesc(Faculty faculty);

    // 4. Faculty Search (Now also searches Username and Display Name)
    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    @Query("SELECT p FROM Post p WHERE p.faculty = :faculty AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.user.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.user.displayName) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") ORDER BY p.createdAt DESC")
    List<Post> searchPostsInFaculty(@Param("query") String query, @Param("faculty") Faculty faculty);
}
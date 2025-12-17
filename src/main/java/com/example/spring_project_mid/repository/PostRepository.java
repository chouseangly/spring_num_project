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

    // --- FOR OWNER / ADMIN (Shows everything) ---
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

    // --- FOR PUBLIC VIEW (Hides suspended posts, treats NULL as active) ---

    // 1. For Homepage
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE (p.suspended = false OR p.suspended IS NULL) ORDER BY p.createdAt DESC")
    List<Post> findAllBySuspendedFalseOrderByCreatedAtDesc();

    // 2. For Visiting other profiles
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE p.user = :user AND (p.suspended = false OR p.suspended IS NULL) ORDER BY p.createdAt DESC")
    List<Post> findAllByUserAndSuspendedFalseOrderByCreatedAtDesc(@Param("user") User user);

    // 3. Updated Search: Title Starts With Query (Prefix Match) & NULL Suspended Handling
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments", "images", "savedPosts"})
    @Query("SELECT p FROM Post p WHERE " +
            "(LOWER(p.title) LIKE LOWER(CONCAT(:query, '%')) OR " +  // Title: Starts With
            " LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " + // Content: Contains
            "AND (p.suspended = false OR p.suspended IS NULL) " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@Param("query") String query);

    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    List<Post> findAllByFacultyOrderByCreatedAtDesc(Faculty faculty);

    // 4. Faculty Search: Title Starts With Query
    @EntityGraph(attributePaths = {"user", "faculty", "images"})
    @Query("SELECT p FROM Post p WHERE p.faculty = :faculty AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT(:keyword, '%')) OR " + // Title: Starts With
            " LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchPostsInFaculty(@Param("keyword") String keyword, @Param("faculty") Faculty faculty);
}
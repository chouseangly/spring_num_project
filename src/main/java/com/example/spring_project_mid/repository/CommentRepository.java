package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Comment;
import com.example.spring_project_mid.model.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"user", "post"})
    List<Comment> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "post"})
    List<Comment> findByUserAndSuspendedFalseOrderByCreatedAtDesc(User user);
}
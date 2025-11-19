package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Comment;
import com.example.spring_project_mid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUserOrderByCreatedAtDesc(User user);
}
package com.example.spring_project_mid.repository;


import com.example.spring_project_mid.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentRepository extends JpaRepository<Comment, Long> {}
package com.example.spring_project_mid.repository;


import com.example.spring_project_mid.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- Import List

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments"})
    List<Post> findAllByOrderByCreatedAtDesc();
}
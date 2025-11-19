package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Post;
import com.example.spring_project_mid.model.SavedPost;
import com.example.spring_project_mid.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    Optional<SavedPost> findByUserAndPost(User user, Post post);

    List<SavedPost> findByUserOrderByCreatedAtDesc(User user);
}
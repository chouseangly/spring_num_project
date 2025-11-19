package com.example.spring_project_mid.repository;
import com.example.spring_project_mid.model.Vote;
import com.example.spring_project_mid.model.Post; // <-- ADD IMPORT
import com.example.spring_project_mid.model.User; // <-- ADD IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // <-- ADD IMPORT

public interface VoteRepository extends JpaRepository<Vote, Long> {
    // New method to find an existing like (Vote) by User and Post
    Optional<Vote> findByUserAndPost(User user, Post post);
}
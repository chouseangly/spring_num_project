package com.example.spring_project_mid.repository;
import com.example.spring_project_mid.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VoteRepository extends JpaRepository<Vote, Long> {}
package com.example.spring_project_mid.repository;


import com.example.spring_project_mid.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- Import List

public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = {"user", "faculty", "votes", "comments"})
    List<Event> findAllByOrderByCreatedAtDesc();
}
package com.example.spring_project_mid.repository;


import com.example.spring_project_mid.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- Import List

public interface EventRepository extends JpaRepository<Event, Long> {

    // --- ADD THIS METHOD ---
    /**
     * Finds all Events, ordered by their creation time in descending order (newest first).
     */
    List<Event> findAllByOrderByCreatedAtDesc();
}
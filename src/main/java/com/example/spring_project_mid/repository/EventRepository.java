package com.example.spring_project_mid.repository;


import com.example.spring_project_mid.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventRepository extends JpaRepository<Event, Long> {}
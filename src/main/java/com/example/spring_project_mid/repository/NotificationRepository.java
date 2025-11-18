package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Notification;
import com.example.spring_project_mid.model.User; // <-- ADD IMPORT
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // <-- ADD IMPORT

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
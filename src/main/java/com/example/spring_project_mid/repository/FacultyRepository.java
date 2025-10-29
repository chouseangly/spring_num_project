package com.example.spring_project_mid.repository;
import com.example.spring_project_mid.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FacultyRepository extends JpaRepository<Faculty, Long> {}
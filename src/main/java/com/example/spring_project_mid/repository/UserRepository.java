package com.example.spring_project_mid.repository;

import com.example.spring_project_mid.model.User;
import com.example.spring_project_mid.model.enums.Role;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByVerificationOtp(String otp);
    Optional<User> findByResetPasswordToken(String token);
    List<User> findAllByRoleNotOrderByIdAsc(Role role);
    List<User> findByRoleNot(Role role, Sort sort);

    // Enhanced search method with role and status filters
    @Query("SELECT u FROM User u WHERE u.role <> 'SUPER_ADMIN' " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "    LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "    LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.enabled = :status)")
    List<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") Role role,
                           @Param("status") Boolean status,
                           Sort sort);
}
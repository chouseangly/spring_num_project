package com.example.spring_project_mid.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet; // <-- ADD IMPORT
import java.util.Set;     // <-- ADD IMPORT

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // For text posts

    private String linkUrl; // For link posts

    @Column(columnDefinition = "TEXT")
    private String mediaUrls; // Comma-separated IPFS gateway URLs for images/videos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // <-- ADD: Prevents recursive toString()
    @EqualsAndHashCode.Exclude // <-- ADD: Prevents recursive hashCode()
    private User user;

    // --- START: ADD THIS 'FACULTY' RELATIONSHIP BACK ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id") // This column must exist in your 'post' table
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Faculty faculty;
    // --- END: 'FACULTY' RELATIONSHIP ---

    // --- START: ADD 'COMMENTS' RELATIONSHIP BACK ---
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Comment> comments = new HashSet<>();
    // --- END: 'COMMENTS' RELATIONSHIP ---

    // --- START: ADD 'VOTES' RELATIONSHIP BACK ---
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Vote> votes = new HashSet<>();
    // --- END: 'VOTES' RELATIONSHIP ---

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
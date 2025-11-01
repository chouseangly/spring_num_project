// In: chouseangly/spring_num_project/spring_num_project-main/src/main/java/com/example/spring_project_mid/model/Post.java

package com.example.spring_project_mid.model;

import jakarta.persistence.*;
import lombok.*; // Keep this import

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
// --- START OF FIX ---
@Getter // Replace @Data with @Getter
@Setter // and @Setter
@ToString // and @ToString
// --- @Data annotation removed ---
// --- END OF FIX ---
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts") // <-- FIX: Changed from "events" to "posts"
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // For text posts

    private String linkUrl; // For link posts

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // This will now be used by the class-level @ToString
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    @ToString.Exclude // This will now be used by the class-level @ToString
    @EqualsAndHashCode.Exclude
    private Faculty faculty;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // This will now be used by the class-level @ToString
    @EqualsAndHashCode.Exclude
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // This will now be used by the class-level @ToString
    @EqualsAndHashCode.Exclude
    private Set<Vote> votes = new HashSet<>();

    // --- ADDED THIS ---
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // This will now be used by the class-level @ToString
    @EqualsAndHashCode.Exclude
    private Set<Image> images = new HashSet<>();
    // --- END ADD ---

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
package com.example.spring_project_mid.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "votes", uniqueConstraints = {
        // --- CHANGE 'event_id' to 'post_id' ---
        @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // --- CHANGE 'event_id' to 'post_id' and RENAME 'event' to 'post' ---
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "vote_type", nullable = false)
    private Integer voteType; // 1 for upvote, -1 for downvote

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
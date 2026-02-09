package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records when a student has reviewed/acknowledged a training material.
 * E-signed with the student's registered signature profile for authentication.
 */
@Entity
@Table(name = "material_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "material_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private TrainingMaterial material;

    @CreationTimestamp
    @Column(name = "reviewed_at", updatable = false)
    private LocalDateTime reviewedAt;
}

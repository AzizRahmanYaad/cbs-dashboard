package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "training_capacity_buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCapacityBuilding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;
    
    @Column(name = "training_type", length = 50, nullable = false)
    private String trainingType; // internal/external
    
    @Column(name = "topic", length = 500, nullable = false)
    private String topic;
    
    @Column(name = "duration", length = 100)
    private String duration;
    
    @Column(name = "skills_gained", columnDefinition = "TEXT")
    private String skillsGained;
    
    @Column(name = "trainer_name", length = 200)
    private String trainerName;
    
    @Column(name = "participants", columnDefinition = "TEXT")
    private String participants; // comma-separated or JSON
}


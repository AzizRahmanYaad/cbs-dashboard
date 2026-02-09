package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private TrainingProgram program;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "material_type", length = 50)
    private String materialType; // PDF, Video, Document, Link, Presentation
    
    @Column(name = "file_path", length = 500)
    private String filePath; // URL or file system path
    
    @Column(name = "file_size")
    private Long fileSize; // in bytes
    
    @Column(name = "file_name", length = 255)
    private String fileName;
    
    @Column(name = "is_required")
    private Boolean isRequired = false;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

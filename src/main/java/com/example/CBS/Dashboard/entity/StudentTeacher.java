package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentTeacher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type; // STUDENT or TEACHER
    
    @Column(name = "employee_id", length = 50)
    private String employeeId;
    
    @Column(name = "student_id", length = 50)
    private String studentId;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "qualification", length = 200)
    private String qualification;
    
    @Column(name = "specialization", length = 200)
    private String specialization;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Type {
        STUDENT, TEACHER
    }
}

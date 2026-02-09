package com.example.CBS.Dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    
    @Column(name = "attendance_date")
    private LocalDateTime attendanceDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by_id", nullable = false)
    private User markedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** E-signature image (base64) stored when status is PRESENT or after student acknowledgment for ABSENT/EXCUSED. */
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData;

    /** PRESENT = signature attached when teacher marked present; ACKNOWLEDGMENT = student signed after reviewing materials. */
    @Enumerated(EnumType.STRING)
    @Column(name = "signature_type", length = 20)
    private SignatureType signatureType;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED
    }

    public enum SignatureType {
        PRESENT,   // Signature used when marked Present
        ACKNOWLEDGMENT  // Student signed to acknowledge materials (Absent/Excused)
    }
}

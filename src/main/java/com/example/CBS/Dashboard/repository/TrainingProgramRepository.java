package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {
    // Return all programs ordered by most recently created first
    List<TrainingProgram> findAllByOrderByCreatedAtDesc();
    
    List<TrainingProgram> findByStatus(TrainingProgram.TrainingStatus status);
    
    List<TrainingProgram> findByCategory(String category);
    
    List<TrainingProgram> findByCreatedById(Long createdById);
    
    List<TrainingProgram> findByInstructorId(Long instructorId);
    
    @Query("SELECT DISTINCT tp FROM TrainingProgram tp " +
           "JOIN tp.enrollments e " +
           "WHERE e.participant.id = :studentId " +
           "AND e.status != 'WITHDRAWN' " +
           "AND e.status != 'CANCELLED'")
    List<TrainingProgram> findByStudentId(@Param("studentId") Long studentId);
}

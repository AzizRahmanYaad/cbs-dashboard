package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    @Query("SELECT DISTINCT ts FROM TrainingSession ts " +
           "LEFT JOIN FETCH ts.program " +
           "LEFT JOIN FETCH ts.instructor " +
           "LEFT JOIN FETCH ts.createdBy " +
           "ORDER BY ts.sequenceOrder DESC NULLS LAST, ts.id DESC")
    @Override
    List<TrainingSession> findAll();
    
    @Query("SELECT DISTINCT ts FROM TrainingSession ts " +
           "LEFT JOIN FETCH ts.program " +
           "LEFT JOIN FETCH ts.instructor " +
           "WHERE ts.program.id = :programId " +
           "ORDER BY ts.sequenceOrder DESC NULLS LAST, ts.id DESC")
    List<TrainingSession> findByProgramId(@Param("programId") Long programId);
    
    @Query("SELECT DISTINCT ts FROM TrainingSession ts " +
           "LEFT JOIN FETCH ts.program " +
           "LEFT JOIN FETCH ts.instructor " +
           "WHERE ts.status = :status")
    List<TrainingSession> findByStatus(@Param("status") TrainingSession.SessionStatus status);
    
    @Query("SELECT DISTINCT ts FROM TrainingSession ts " +
           "LEFT JOIN FETCH ts.program " +
           "LEFT JOIN FETCH ts.instructor " +
           "WHERE ts.startDateTime >= :startDate AND ts.endDateTime <= :endDate")
    List<TrainingSession> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT ts FROM TrainingSession ts " +
           "LEFT JOIN FETCH ts.program " +
           "LEFT JOIN FETCH ts.instructor " +
           "WHERE (ts.instructor.id = :instructorId OR ts.program.instructor.id = :instructorId) " +
           "AND ts.startDateTime >= :from AND ts.startDateTime < :to " +
           "ORDER BY ts.startDateTime ASC")
    List<TrainingSession> findByInstructorIdAndDateRange(@Param("instructorId") Long instructorId,
                                                         @Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);
}

package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT DISTINCT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.session " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH e.enrolledBy")
    @Override
    List<Enrollment> findAll();
    
    @Query("SELECT DISTINCT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.session " +
           "LEFT JOIN FETCH e.participant " +
           "WHERE e.program.id = :programId")
    List<Enrollment> findByProgramId(@Param("programId") Long programId);
    
    @Query("SELECT DISTINCT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.session " +
           "LEFT JOIN FETCH e.participant " +
           "WHERE e.session.id = :sessionId")
    List<Enrollment> findBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT DISTINCT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.session " +
           "LEFT JOIN FETCH e.participant " +
           "WHERE e.participant.id = :participantId")
    List<Enrollment> findByParticipantId(@Param("participantId") Long participantId);
    
    @Query("SELECT DISTINCT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.session " +
           "LEFT JOIN FETCH e.participant " +
           "WHERE e.program.id = :programId AND e.participant.id = :participantId")
    Optional<Enrollment> findByProgramIdAndParticipantId(@Param("programId") Long programId, 
                                                          @Param("participantId") Long participantId);
    
    @Query("SELECT DISTINCT e FROM Enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.session " +
           "LEFT JOIN FETCH e.participant " +
           "WHERE e.status = :status")
    List<Enrollment> findByStatus(@Param("status") Enrollment.EnrollmentStatus status);
}

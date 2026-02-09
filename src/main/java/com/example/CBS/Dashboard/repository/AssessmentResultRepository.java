package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    @Query("SELECT DISTINCT ar FROM AssessmentResult ar " +
           "LEFT JOIN FETCH ar.assessment " +
           "LEFT JOIN FETCH ar.participant " +
           "LEFT JOIN FETCH ar.gradedBy")
    @Override
    List<AssessmentResult> findAll();
    
    @Query("SELECT DISTINCT ar FROM AssessmentResult ar " +
           "LEFT JOIN FETCH ar.assessment " +
           "LEFT JOIN FETCH ar.participant " +
           "LEFT JOIN FETCH ar.gradedBy " +
           "WHERE ar.assessment.id = :assessmentId")
    List<AssessmentResult> findByAssessmentId(@Param("assessmentId") Long assessmentId);
    
    @Query("SELECT DISTINCT ar FROM AssessmentResult ar " +
           "LEFT JOIN FETCH ar.assessment " +
           "LEFT JOIN FETCH ar.participant " +
           "LEFT JOIN FETCH ar.gradedBy " +
           "WHERE ar.participant.id = :participantId")
    List<AssessmentResult> findByParticipantId(@Param("participantId") Long participantId);
    
    @Query("SELECT DISTINCT ar FROM AssessmentResult ar " +
           "LEFT JOIN FETCH ar.assessment " +
           "LEFT JOIN FETCH ar.participant " +
           "LEFT JOIN FETCH ar.gradedBy " +
           "WHERE ar.assessment.id = :assessmentId AND ar.participant.id = :participantId " +
           "ORDER BY ar.attemptNumber DESC")
    List<AssessmentResult> findByAssessmentIdAndParticipantId(@Param("assessmentId") Long assessmentId, 
                                                              @Param("participantId") Long participantId);
    
    @Query("SELECT DISTINCT ar FROM AssessmentResult ar " +
           "LEFT JOIN FETCH ar.assessment " +
           "LEFT JOIN FETCH ar.participant " +
           "LEFT JOIN FETCH ar.gradedBy " +
           "WHERE ar.assessment.id = :assessmentId AND ar.participant.id = :participantId " +
           "ORDER BY ar.attemptNumber DESC")
    Optional<AssessmentResult> findLatestByAssessmentIdAndParticipantId(@Param("assessmentId") Long assessmentId, 
                                                                        @Param("participantId") Long participantId);
}

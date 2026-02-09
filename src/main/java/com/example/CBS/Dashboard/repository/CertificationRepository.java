package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    @Query("SELECT DISTINCT c FROM Certification c " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH c.issuedBy")
    @Override
    List<Certification> findAll();
    
    @Query("SELECT DISTINCT c FROM Certification c " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH c.issuedBy " +
           "WHERE c.certificateNumber = :certificateNumber")
    Optional<Certification> findByCertificateNumber(@Param("certificateNumber") String certificateNumber);
    
    @Query("SELECT DISTINCT c FROM Certification c " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH c.issuedBy " +
           "WHERE e.participant.id = :participantId")
    List<Certification> findByParticipantId(@Param("participantId") Long participantId);
    
    @Query("SELECT DISTINCT c FROM Certification c " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH c.issuedBy " +
           "WHERE e.program.id = :programId")
    List<Certification> findByProgramId(@Param("programId") Long programId);
    
    @Query("SELECT DISTINCT c FROM Certification c " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH c.issuedBy " +
           "WHERE c.expiryDate <= :date AND c.isValid = true")
    List<Certification> findExpiringCertifications(@Param("date") LocalDate date);
    
    @Query("SELECT DISTINCT c FROM Certification c " +
           "LEFT JOIN FETCH c.enrollment e " +
           "LEFT JOIN FETCH e.program " +
           "LEFT JOIN FETCH e.participant " +
           "LEFT JOIN FETCH c.issuedBy " +
           "WHERE c.isValid = :isValid")
    List<Certification> findByIsValid(@Param("isValid") Boolean isValid);
}

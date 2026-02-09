package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    @Query("SELECT DISTINCT a FROM Assessment a " +
           "LEFT JOIN FETCH a.program " +
           "LEFT JOIN FETCH a.createdBy")
    @Override
    List<Assessment> findAll();
    
    @Query("SELECT DISTINCT a FROM Assessment a " +
           "LEFT JOIN FETCH a.program " +
           "LEFT JOIN FETCH a.createdBy " +
           "WHERE a.program.id = :programId")
    List<Assessment> findByProgramId(@Param("programId") Long programId);
    
    @Query("SELECT DISTINCT a FROM Assessment a " +
           "LEFT JOIN FETCH a.program " +
           "LEFT JOIN FETCH a.createdBy " +
           "WHERE a.status = :status")
    List<Assessment> findByStatus(@Param("status") Assessment.AssessmentStatus status);
}

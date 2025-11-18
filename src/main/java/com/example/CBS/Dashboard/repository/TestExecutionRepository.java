package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TestExecution;
import com.example.CBS.Dashboard.entity.TestExecution.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    @Query("SELECT DISTINCT te FROM TestExecution te " +
           "LEFT JOIN FETCH te.testCase " +
           "LEFT JOIN FETCH te.executedBy " +
           "WHERE te.testCase.id = :testCaseId")
    List<TestExecution> findByTestCaseId(@Param("testCaseId") Long testCaseId);
    
    @Query("SELECT DISTINCT te FROM TestExecution te " +
           "LEFT JOIN FETCH te.testCase " +
           "LEFT JOIN FETCH te.executedBy " +
           "WHERE te.executedBy.id = :executedById")
    List<TestExecution> findByExecutedById(@Param("executedById") Long executedById);
    
    @Query("SELECT DISTINCT te FROM TestExecution te " +
           "LEFT JOIN FETCH te.testCase " +
           "LEFT JOIN FETCH te.executedBy " +
           "WHERE te.status = :status")
    List<TestExecution> findByStatus(@Param("status") ExecutionStatus status);
    
    @Query("SELECT DISTINCT te FROM TestExecution te " +
           "LEFT JOIN FETCH te.testCase " +
           "LEFT JOIN FETCH te.executedBy " +
           "WHERE te.executedAt BETWEEN :startDate AND :endDate")
    List<TestExecution> findByExecutionDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT DISTINCT te FROM TestExecution te " +
           "LEFT JOIN FETCH te.testCase " +
           "LEFT JOIN FETCH te.executedBy " +
           "WHERE te.testCase.module.id = :moduleId")
    List<TestExecution> findByModuleId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT DISTINCT te FROM TestExecution te " +
           "LEFT JOIN FETCH te.testCase " +
           "LEFT JOIN FETCH te.executedBy")
    @Override
    List<TestExecution> findAll();
}


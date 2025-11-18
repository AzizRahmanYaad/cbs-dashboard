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
    List<TestExecution> findByTestCaseId(Long testCaseId);
    List<TestExecution> findByExecutedById(Long executedById);
    List<TestExecution> findByStatus(ExecutionStatus status);
    
    @Query("SELECT te FROM TestExecution te WHERE te.executedAt BETWEEN :startDate AND :endDate")
    List<TestExecution> findByExecutionDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT te FROM TestExecution te WHERE te.testCase.module.id = :moduleId")
    List<TestExecution> findByModuleId(@Param("moduleId") Long moduleId);
}


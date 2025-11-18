package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TestCase;
import com.example.CBS.Dashboard.entity.TestCase.TestCaseStatus;
import com.example.CBS.Dashboard.entity.TestCase.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByModuleId(Long moduleId);
    List<TestCase> findByAssignedToId(Long assignedToId);
    List<TestCase> findByStatus(TestCaseStatus status);
    List<TestCase> findByPriority(Priority priority);
    List<TestCase> findByCreatedById(Long createdById);
    
    @Query("SELECT tc FROM TestCase tc WHERE " +
           "(:moduleId IS NULL OR tc.module.id = :moduleId) AND " +
           "(:status IS NULL OR tc.status = :status) AND " +
           "(:assignedToId IS NULL OR tc.assignedTo.id = :assignedToId) AND " +
           "(:priority IS NULL OR tc.priority = :priority)")
    List<TestCase> findByFilters(
        @Param("moduleId") Long moduleId,
        @Param("status") TestCaseStatus status,
        @Param("assignedToId") Long assignedToId,
        @Param("priority") Priority priority
    );
    
    @Query("SELECT tc FROM TestCase tc WHERE " +
           "LOWER(tc.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(tc.preconditions) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TestCase> searchByTitleOrPreconditions(@Param("searchTerm") String searchTerm);
}


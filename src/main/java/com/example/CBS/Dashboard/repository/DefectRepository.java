package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Defect;
import com.example.CBS.Dashboard.entity.Defect.DefectStatus;
import com.example.CBS.Dashboard.entity.Defect.DefectSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {
    List<Defect> findByTestCaseId(Long testCaseId);
    List<Defect> findByTestExecutionId(Long testExecutionId);
    List<Defect> findByStatus(DefectStatus status);
    List<Defect> findBySeverity(DefectSeverity severity);
    List<Defect> findByAssignedToId(Long assignedToId);
    List<Defect> findByReportedById(Long reportedById);
    
    @Query("SELECT d FROM Defect d WHERE " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:severity IS NULL OR d.severity = :severity) AND " +
           "(:assignedToId IS NULL OR d.assignedTo.id = :assignedToId)")
    List<Defect> findByFilters(
        @Param("status") DefectStatus status,
        @Param("severity") DefectSeverity severity,
        @Param("assignedToId") Long assignedToId
    );
}


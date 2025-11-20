package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.DailyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long>, JpaSpecificationExecutor<DailyReport> {
    
    Optional<DailyReport> findByBusinessDateAndEmployeeId(LocalDate businessDate, Long employeeId);
    
    List<DailyReport> findByEmployeeIdOrderByBusinessDateDesc(Long employeeId);
    
    Page<DailyReport> findByEmployeeIdOrderByBusinessDateDesc(Long employeeId, Pageable pageable);
    
    Page<DailyReport> findByStatusOrderByBusinessDateDesc(DailyReport.ReportStatus status, Pageable pageable);
    
    @Query("SELECT dr FROM DailyReport dr WHERE dr.businessDate BETWEEN :startDate AND :endDate ORDER BY dr.businessDate DESC")
    List<DailyReport> findByBusinessDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT dr FROM DailyReport dr WHERE dr.businessDate = :date ORDER BY dr.employee.username")
    List<DailyReport> findByBusinessDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(dr) FROM DailyReport dr WHERE dr.status = :status")
    Long countByStatus(@Param("status") DailyReport.ReportStatus status);
    
    @Query("SELECT dr FROM DailyReport dr WHERE dr.employee.id = :employeeId AND dr.businessDate = :date")
    Optional<DailyReport> findByEmployeeIdAndBusinessDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}


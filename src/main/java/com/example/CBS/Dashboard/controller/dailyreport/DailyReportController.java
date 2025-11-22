package com.example.CBS.Dashboard.controller.dailyreport;

import com.example.CBS.Dashboard.dto.dailyreport.*;
import com.example.CBS.Dashboard.entity.DailyReport;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.UserRepository;
import com.example.CBS.Dashboard.service.dailyreport.DailyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-reports")
public class DailyReportController {
    
    @Autowired
    private DailyReportService dailyReportService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<DailyReportDto> createReport(
            @RequestBody CreateDailyReportRequest request,
            Authentication authentication) {
        Long employeeId = getUserIdFromAuthentication(authentication);
        DailyReportDto report = dailyReportService.createReport(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DailyReportDto> updateReport(
            @PathVariable Long id,
            @RequestBody UpdateDailyReportRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        DailyReportDto report = dailyReportService.updateReport(id, userId, request);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<DailyReportDto> submitReport(
            @PathVariable Long id,
            Authentication authentication) {
        Long employeeId = getUserIdFromAuthentication(authentication);
        DailyReportDto report = dailyReportService.submitReport(id, employeeId);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_ADMIN')")
    public ResponseEntity<DailyReportDto> reviewReport(
            @PathVariable Long id,
            @RequestBody ReviewReportRequest request,
            Authentication authentication) {
        Long reviewerId = getUserIdFromAuthentication(authentication);
        DailyReportDto report = dailyReportService.reviewReport(id, reviewerId, request);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DailyReportDto> getReport(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        DailyReportDto report = dailyReportService.getReport(id, userId);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<DailyReportDto> getReportByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long employeeId = getUserIdFromAuthentication(authentication);
        DailyReportDto report = dailyReportService.getReportByDate(date, employeeId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/my-reports")
    public ResponseEntity<Page<DailyReportDto>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "businessDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {
        Long employeeId = getUserIdFromAuthentication(authentication);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DailyReportDto> reports = dailyReportService.getMyReports(employeeId, pageable);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_DAILY_REPORT_DIRECTOR', 'ROLE_DAILY_REPORT_MANAGER', 'ROLE_DAILY_REPORT_TEAM_LEAD', 'ROLE_ADMIN')")
    public ResponseEntity<Page<DailyReportDto>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "businessDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) DailyReport.ReportStatus status) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DailyReportDto> reports = dailyReportService.getAllReports(pageable, startDate, endDate, employeeId, status);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_DAILY_REPORT_DIRECTOR', 'ROLE_DAILY_REPORT_MANAGER', 'ROLE_DAILY_REPORT_TEAM_LEAD', 'ROLE_ADMIN')")
    public ResponseEntity<DailyReportDashboardDto> getDashboard() {
        DailyReportDashboardDto dashboard = dailyReportService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        dailyReportService.deleteReport(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/download/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_ADMIN')")
    public ResponseEntity<String> downloadEmployeeReport(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String reportContent = dailyReportService.generateEmployeeReport(employeeId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "employee_report_" + employeeId + ".txt");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(reportContent);
    }
    
    @GetMapping("/download/combined")
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_ADMIN')")
    public ResponseEntity<String> downloadCombinedReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) LocalDate specificDate) {
        String reportContent = dailyReportService.generateCombinedReport(startDate, endDate, specificDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        String filename = specificDate != null ? 
            "combined_report_" + specificDate + ".txt" : 
            "combined_report_" + (startDate != null ? startDate : "all") + ".txt";
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(reportContent);
    }
    
    @GetMapping("/by-date/{date}")
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_DAILY_REPORT_DIRECTOR', 'ROLE_DAILY_REPORT_MANAGER', 'ROLE_DAILY_REPORT_TEAM_LEAD', 'ROLE_ADMIN')")
    public ResponseEntity<List<DailyReportDto>> getReportsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyReportDto> reports = dailyReportService.getReportsByDate(date);
        return ResponseEntity.ok(reports);
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("User not authenticated");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        return user.getId();
    }
}


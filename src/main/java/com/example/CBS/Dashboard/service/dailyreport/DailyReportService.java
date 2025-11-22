package com.example.CBS.Dashboard.service.dailyreport;

import com.example.CBS.Dashboard.dto.dailyreport.*;
import com.example.CBS.Dashboard.entity.DailyReport;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.entity.ChatCommunication;
import com.example.CBS.Dashboard.entity.EmailCommunication;
import com.example.CBS.Dashboard.entity.ProblemEscalation;
import com.example.CBS.Dashboard.entity.TrainingCapacityBuilding;
import com.example.CBS.Dashboard.entity.ProjectProgressUpdate;
import com.example.CBS.Dashboard.entity.CbsTeamActivity;
import com.example.CBS.Dashboard.entity.PendingActivity;
import com.example.CBS.Dashboard.entity.Meeting;
import com.example.CBS.Dashboard.entity.AfpayCardRequest;
import com.example.CBS.Dashboard.entity.QrmisIssue;
import com.example.CBS.Dashboard.mapper.DailyReportMapper;
import com.example.CBS.Dashboard.repository.DailyReportRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyReportService {
    
    @Autowired
    private DailyReportRepository dailyReportRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DailyReportMapper dailyReportMapper;
    
    @Transactional
    public DailyReportDto createReport(Long employeeId, CreateDailyReportRequest request) {
        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        
        // Check if report already exists for this date
        LocalDate businessDate = request.getBusinessDate() != null ? 
            request.getBusinessDate() : LocalDate.now();
        
        if (dailyReportRepository.findByBusinessDateAndEmployeeId(businessDate, employeeId).isPresent()) {
            throw new IllegalArgumentException("Report already exists for this business date");
        }
        
        DailyReport report = new DailyReport();
        report.setBusinessDate(businessDate);
        report.setEmployee(employee);
        report.setCbsEndTime(request.getCbsEndTime());
        report.setCbsStartTimeNextDay(request.getCbsStartTimeNextDay());
        report.setReportingLine(request.getReportingLine());
        report.setStatus(DailyReport.ReportStatus.DRAFT);
        
        // Create and add all section entities
        addChatCommunications(report, request.getChatCommunications());
        addEmailCommunications(report, request.getEmailCommunications());
        addProblemEscalations(report, request.getProblemEscalations());
        addTrainingCapacityBuildings(report, request.getTrainingCapacityBuildings());
        addProjectProgressUpdates(report, request.getProjectProgressUpdates());
        addCbsTeamActivities(report, request.getCbsTeamActivities());
        addPendingActivities(report, request.getPendingActivities());
        addMeetings(report, request.getMeetings());
        addAfpayCardRequests(report, request.getAfpayCardRequests());
        addQrmisIssues(report, request.getQrmisIssues());
        
        report = dailyReportRepository.save(report);
        return dailyReportMapper.toDto(report);
    }
    
    @Transactional
    public DailyReportDto updateReport(Long reportId, Long employeeId, UpdateDailyReportRequest request) {
        DailyReport report = dailyReportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        // Check ownership or supervisor access
        if (!report.getEmployee().getId().equals(employeeId) && !hasSupervisorAccess(employeeId)) {
            throw new SecurityException("You don't have permission to edit this report");
        }
        
        // If approved, require supervisor re-approval
        if (report.getStatus() == DailyReport.ReportStatus.APPROVED) {
            report.setStatus(DailyReport.ReportStatus.SUBMITTED);
        }
        
        dailyReportMapper.updateEntityFromDto(report, request);
        report = dailyReportRepository.save(report);
        return dailyReportMapper.toDto(report);
    }
    
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_ADMIN')")
    public DailyReportDto reviewReport(Long reportId, Long reviewerId, ReviewReportRequest request) {
        DailyReport report = dailyReportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new EntityNotFoundException("Reviewer not found"));
        
        report.setStatus(request.getStatus());
        report.setReviewedBy(reviewer);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewComments(request.getReviewComments());
        
        report = dailyReportRepository.save(report);
        return dailyReportMapper.toDto(report);
    }
    
    @Transactional
    public DailyReportDto submitReport(Long reportId, Long employeeId) {
        DailyReport report = dailyReportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        if (!report.getEmployee().getId().equals(employeeId)) {
            throw new SecurityException("You can only submit your own reports");
        }
        
        // Validate required fields
        validateReport(report);
        
        report.setStatus(DailyReport.ReportStatus.SUBMITTED);
        report = dailyReportRepository.save(report);
        return dailyReportMapper.toDto(report);
    }
    
    @Transactional(readOnly = true)
    public DailyReportDto getReport(Long reportId, Long userId) {
        DailyReport report = dailyReportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        // Check access: owner or supervisor
        if (!report.getEmployee().getId().equals(userId) && !hasSupervisorAccess(userId)) {
            throw new SecurityException("You don't have permission to view this report");
        }
        
        return dailyReportMapper.toDto(report);
    }
    
    @Transactional(readOnly = true)
    public DailyReportDto getReportByDate(LocalDate businessDate, Long employeeId) {
        return dailyReportRepository.findByBusinessDateAndEmployeeId(businessDate, employeeId)
            .map(dailyReportMapper::toDto)
            .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Page<DailyReportDto> getMyReports(Long employeeId, Pageable pageable) {
        // Get paginated reports
        Page<DailyReport> reportsPage = dailyReportRepository.findByEmployeeIdOrderByBusinessDateDesc(employeeId, pageable);
        
        // For each report, initialize collections to avoid lazy loading issues
        reportsPage.getContent().forEach(report -> {
            // Initialize all collections by accessing them
            if (report.getChatCommunications() != null) report.getChatCommunications().size();
            if (report.getEmailCommunications() != null) report.getEmailCommunications().size();
            if (report.getProblemEscalations() != null) report.getProblemEscalations().size();
            if (report.getTrainingCapacityBuildings() != null) report.getTrainingCapacityBuildings().size();
            if (report.getProjectProgressUpdates() != null) report.getProjectProgressUpdates().size();
            if (report.getCbsTeamActivities() != null) report.getCbsTeamActivities().size();
            if (report.getPendingActivities() != null) report.getPendingActivities().size();
            if (report.getMeetings() != null) report.getMeetings().size();
            if (report.getAfpayCardRequests() != null) report.getAfpayCardRequests().size();
            if (report.getQrmisIssues() != null) report.getQrmisIssues().size();
            // Also initialize employee and reviewedBy
            if (report.getEmployee() != null) report.getEmployee().getUsername();
            if (report.getReviewedBy() != null) report.getReviewedBy().getUsername();
        });
        
        return reportsPage.map(dailyReportMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_DAILY_REPORT_DIRECTOR', 'ROLE_DAILY_REPORT_MANAGER', 'ROLE_DAILY_REPORT_TEAM_LEAD', 'ROLE_ADMIN')")
    public Page<DailyReportDto> getAllReports(Pageable pageable, LocalDate startDate, LocalDate endDate, 
                                               Long employeeId, DailyReport.ReportStatus status) {
        Specification<DailyReport> spec = null;
        
        if (startDate != null && endDate != null) {
            Specification<DailyReport> dateSpec = (root, query, cb) -> 
                cb.between(root.get("businessDate"), startDate, endDate);
            spec = spec == null ? dateSpec : spec.and(dateSpec);
        }
        
        if (employeeId != null) {
            Specification<DailyReport> employeeSpec = (root, query, cb) -> 
                cb.equal(root.get("employee").get("id"), employeeId);
            spec = spec == null ? employeeSpec : spec.and(employeeSpec);
        }
        
        if (status != null) {
            Specification<DailyReport> statusSpec = (root, query, cb) -> 
                cb.equal(root.get("status"), status);
            spec = spec == null ? statusSpec : spec.and(statusSpec);
        }
        
        if (spec == null) {
            return dailyReportRepository.findAll(pageable)
                .map(dailyReportMapper::toDto);
        }
        
        return dailyReportRepository.findAll(spec, pageable)
            .map(dailyReportMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_DAILY_REPORT_SUPERVISOR', 'ROLE_DAILY_REPORT_DIRECTOR', 'ROLE_DAILY_REPORT_MANAGER', 'ROLE_DAILY_REPORT_TEAM_LEAD', 'ROLE_ADMIN')")
    public DailyReportDashboardDto getDashboard() {
        DailyReportDashboardDto dashboard = new DailyReportDashboardDto();
        
        long totalReports = dailyReportRepository.count();
        dashboard.setTotalReports(totalReports);
        dashboard.setPendingReports(dailyReportRepository.countByStatus(DailyReport.ReportStatus.SUBMITTED));
        dashboard.setApprovedReports(dailyReportRepository.countByStatus(DailyReport.ReportStatus.APPROVED));
        dashboard.setRejectedReports(dailyReportRepository.countByStatus(DailyReport.ReportStatus.REJECTED));
        dashboard.setDraftReports(dailyReportRepository.countByStatus(DailyReport.ReportStatus.DRAFT));
        
        // Count escalations and pending activities
        List<DailyReport> allReports = dailyReportRepository.findAll();
        long totalEscalations = allReports.stream()
            .mapToLong(r -> r.getProblemEscalations().size())
            .sum();
        long totalPendingActivities = allReports.stream()
            .mapToLong(r -> r.getPendingActivities().size())
            .sum();
        long totalQrmisIssues = allReports.stream()
            .mapToLong(r -> r.getQrmisIssues().size())
            .sum();
        
        dashboard.setTotalEscalations(totalEscalations);
        dashboard.setTotalPendingActivities(totalPendingActivities);
        dashboard.setTotalQrmisIssues(totalQrmisIssues);
        
        // Status distribution
        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("DRAFT", dashboard.getDraftReports());
        statusMap.put("SUBMITTED", dashboard.getPendingReports());
        statusMap.put("APPROVED", dashboard.getApprovedReports());
        statusMap.put("REJECTED", dashboard.getRejectedReports());
        dashboard.setReportsByStatus(statusMap);
        
        return dashboard;
    }
    
    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        DailyReport report = dailyReportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        
        // Only owner or supervisor can delete
        if (!report.getEmployee().getId().equals(userId) && !hasSupervisorAccess(userId)) {
            throw new SecurityException("You don't have permission to delete this report");
        }
        
        dailyReportRepository.delete(report);
    }
    
    // Helper methods to add section entities
    private void addChatCommunications(DailyReport report, List<ChatCommunicationDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                ChatCommunication entity = new ChatCommunication();
                entity.setPlatform(dto.getPlatform());
                entity.setSummary(dto.getSummary());
                entity.setActionTaken(dto.getActionTaken());
                entity.setActionPerformed(dto.getActionPerformed());
                entity.setReferenceNumber(dto.getReferenceNumber());
                entity.setDailyReport(report);
                report.getChatCommunications().add(entity);
            });
        }
    }
    
    private void addEmailCommunications(DailyReport report, List<EmailCommunicationDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                EmailCommunication entity = new EmailCommunication();
                entity.setIsInternal(dto.getIsInternal());
                entity.setSender(dto.getSender());
                entity.setReceiver(dto.getReceiver());
                entity.setSubject(dto.getSubject());
                entity.setSummary(dto.getSummary());
                entity.setActionTaken(dto.getActionTaken());
                entity.setFollowUpRequired(dto.getFollowUpRequired());
                entity.setDailyReport(report);
                report.getEmailCommunications().add(entity);
            });
        }
    }
    
    private void addProblemEscalations(DailyReport report, List<ProblemEscalationDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                ProblemEscalation entity = new ProblemEscalation();
                entity.setEscalatedTo(dto.getEscalatedTo());
                entity.setReason(dto.getReason());
                entity.setEscalationDateTime(dto.getEscalationDateTime());
                entity.setFollowUpStatus(dto.getFollowUpStatus());
                entity.setComments(dto.getComments());
                entity.setDailyReport(report);
                report.getProblemEscalations().add(entity);
            });
        }
    }
    
    private void addTrainingCapacityBuildings(DailyReport report, List<TrainingCapacityBuildingDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                TrainingCapacityBuilding entity = new TrainingCapacityBuilding();
                entity.setTrainingType(dto.getTrainingType());
                entity.setTopic(dto.getTopic());
                entity.setDuration(dto.getDuration());
                entity.setSkillsGained(dto.getSkillsGained());
                entity.setTrainerName(dto.getTrainerName());
                entity.setParticipants(dto.getParticipants());
                entity.setDailyReport(report);
                report.getTrainingCapacityBuildings().add(entity);
            });
        }
    }
    
    private void addProjectProgressUpdates(DailyReport report, List<ProjectProgressUpdateDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                ProjectProgressUpdate entity = new ProjectProgressUpdate();
                entity.setProjectName(dto.getProjectName());
                entity.setTaskOrMilestone(dto.getTaskOrMilestone());
                entity.setProgressDetail(dto.getProgressDetail());
                entity.setRoadblocksIssues(dto.getRoadblocksIssues());
                entity.setEstimatedCompletionDate(dto.getEstimatedCompletionDate());
                entity.setComments(dto.getComments());
                entity.setDailyReport(report);
                report.getProjectProgressUpdates().add(entity);
            });
        }
    }
    
    private void addCbsTeamActivities(DailyReport report, List<CbsTeamActivityDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                CbsTeamActivity entity = new CbsTeamActivity();
                entity.setDescription(dto.getDescription());
                entity.setBranch(dto.getBranch());
                entity.setAccountNumber(dto.getAccountNumber());
                entity.setActionTaken(dto.getActionTaken());
                entity.setFinalStatus(dto.getFinalStatus());
                entity.setActivityType(dto.getActivityType());
                entity.setDailyReport(report);
                report.getCbsTeamActivities().add(entity);
            });
        }
    }
    
    private void addPendingActivities(DailyReport report, List<PendingActivityDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                PendingActivity entity = new PendingActivity();
                entity.setTitle(dto.getTitle());
                entity.setDescription(dto.getDescription());
                entity.setStatus(dto.getStatus());
                entity.setAmount(dto.getAmount());
                entity.setFollowUpRequired(dto.getFollowUpRequired());
                entity.setResponsiblePerson(dto.getResponsiblePerson());
                entity.setDailyReport(report);
                report.getPendingActivities().add(entity);
            });
        }
    }
    
    private void addMeetings(DailyReport report, List<MeetingDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                Meeting entity = new Meeting();
                entity.setMeetingType(dto.getMeetingType());
                entity.setTopic(dto.getTopic());
                entity.setSummary(dto.getSummary());
                entity.setActionTaken(dto.getActionTaken());
                entity.setNextStep(dto.getNextStep());
                entity.setParticipants(dto.getParticipants());
                entity.setDailyReport(report);
                report.getMeetings().add(entity);
            });
        }
    }
    
    private void addAfpayCardRequests(DailyReport report, List<AfpayCardRequestDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                AfpayCardRequest entity = new AfpayCardRequest();
                entity.setRequestType(dto.getRequestType());
                entity.setRequestedBy(dto.getRequestedBy());
                entity.setRequestDate(dto.getRequestDate());
                entity.setResolutionDetails(dto.getResolutionDetails());
                entity.setSupportingDocumentPath(dto.getSupportingDocumentPath());
                entity.setArchivedDate(dto.getArchivedDate());
                entity.setOperator(dto.getOperator());
                entity.setDailyReport(report);
                report.getAfpayCardRequests().add(entity);
            });
        }
    }
    
    private void addQrmisIssues(DailyReport report, List<QrmisIssueDto> dtos) {
        if (dtos != null) {
            dtos.forEach(dto -> {
                QrmisIssue entity = new QrmisIssue();
                entity.setProblemType(dto.getProblemType());
                entity.setProblemDescription(dto.getProblemDescription());
                entity.setSolutionProvided(dto.getSolutionProvided());
                entity.setPostedBy(dto.getPostedBy());
                entity.setAuthorizedBy(dto.getAuthorizedBy());
                entity.setSupportingDocumentsArchived(dto.getSupportingDocumentsArchived());
                entity.setOperator(dto.getOperator());
                entity.setDailyReport(report);
                report.getQrmisIssues().add(entity);
            });
        }
    }
    
    private void validateReport(DailyReport report) {
        if (report.getCbsEndTime() == null || report.getCbsStartTimeNextDay() == null) {
            throw new IllegalArgumentException("CBS Start/End Time is required");
        }
        
        if (report.getCbsTeamActivities().isEmpty()) {
            throw new IllegalArgumentException("At least one CBS Team Activity is required");
        }
    }
    
    @Transactional(readOnly = true)
    public List<DailyReportDto> getReportsByDate(LocalDate date) {
        List<DailyReport> reports = dailyReportRepository.findByBusinessDate(date);
        return reports.stream()
            .map(dailyReportMapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public String generateEmployeeReport(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<DailyReport> reports;
        
        if (startDate != null && endDate != null) {
            Specification<DailyReport> spec = (root, query, cb) -> 
                cb.equal(root.get("employee").get("id"), employeeId);
            spec = spec.and((root, query, cb) -> 
                cb.between(root.get("businessDate"), startDate, endDate)
            );
            reports = dailyReportRepository.findAll(spec);
        } else {
            reports = dailyReportRepository.findByEmployeeIdOrderByBusinessDateDesc(employeeId);
        }
        
        if (reports.isEmpty()) {
            return "No reports found for this employee.";
        }
        
        StringBuilder sb = new StringBuilder();
        User employee = reports.get(0).getEmployee();
        sb.append("=".repeat(80)).append("\n");
        sb.append("DAILY REPORT - ").append(employee.getUsername()).append("\n");
        if (employee.getEmail() != null) {
            sb.append("Email: ").append(employee.getEmail()).append("\n");
        }
        sb.append("=".repeat(80)).append("\n\n");
        
        for (DailyReport report : reports) {
            sb.append(formatReport(report));
            sb.append("\n").append("-".repeat(80)).append("\n\n");
        }
        
        return sb.toString();
    }
    
    @Transactional(readOnly = true)
    public String generateCombinedReport(LocalDate startDate, LocalDate endDate, LocalDate specificDate) {
        List<DailyReport> reports;
        
        if (specificDate != null) {
            reports = dailyReportRepository.findByBusinessDate(specificDate);
        } else if (startDate != null && endDate != null) {
            reports = dailyReportRepository.findByBusinessDateBetween(startDate, endDate);
        } else {
            reports = dailyReportRepository.findAll();
        }
        
        if (reports.isEmpty()) {
            return "No reports found for the specified date range.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append("COMBINED TEAM DAILY REPORT\n");
        if (specificDate != null) {
            sb.append("Date: ").append(specificDate).append("\n");
        } else if (startDate != null && endDate != null) {
            sb.append("Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
        }
        sb.append("Total Reports: ").append(reports.size()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        // Group by date
        Map<LocalDate, List<DailyReport>> reportsByDate = reports.stream()
            .collect(Collectors.groupingBy(DailyReport::getBusinessDate));
        
        reportsByDate.entrySet().stream()
            .sorted(Map.Entry.<LocalDate, List<DailyReport>>comparingByKey().reversed())
            .forEach(entry -> {
                sb.append("\n").append("=".repeat(80)).append("\n");
                sb.append("DATE: ").append(entry.getKey()).append("\n");
                sb.append("=".repeat(80)).append("\n\n");
                
                for (DailyReport report : entry.getValue()) {
                    sb.append(formatReport(report));
                    sb.append("\n").append("-".repeat(80)).append("\n\n");
                }
            });
        
        return sb.toString();
    }
    
    private String formatReport(DailyReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Business Date: ").append(report.getBusinessDate()).append("\n");
        sb.append("Employee: ").append(report.getEmployee().getUsername()).append("\n");
        if (report.getEmployee().getEmail() != null) {
            sb.append("Email: ").append(report.getEmployee().getEmail()).append("\n");
        }
        sb.append("Status: ").append(report.getStatus()).append("\n");
        if (report.getCbsEndTime() != null) {
            sb.append("CBS End Time: ").append(report.getCbsEndTime()).append("\n");
        }
        if (report.getCbsStartTimeNextDay() != null) {
            sb.append("CBS Start Time (Next Day): ").append(report.getCbsStartTimeNextDay()).append("\n");
        }
        if (report.getReportingLine() != null && !report.getReportingLine().isEmpty()) {
            sb.append("Reporting Line: ").append(report.getReportingLine()).append("\n");
        }
        
        if (!report.getCbsTeamActivities().isEmpty()) {
            sb.append("\nCBS Team Activities:\n");
            for (int i = 0; i < report.getCbsTeamActivities().size(); i++) {
                CbsTeamActivity activity = report.getCbsTeamActivities().get(i);
                sb.append("  ").append(i + 1).append(". ").append(activity.getDescription()).append("\n");
                if (activity.getActivityType() != null) {
                    sb.append("     Type: ").append(activity.getActivityType()).append("\n");
                }
                if (activity.getActionTaken() != null) {
                    sb.append("     Action Taken: ").append(activity.getActionTaken()).append("\n");
                }
            }
        }
        
        if (!report.getChatCommunications().isEmpty()) {
            sb.append("\nChat Communications: ").append(report.getChatCommunications().size()).append("\n");
        }
        if (!report.getEmailCommunications().isEmpty()) {
            sb.append("Email Communications: ").append(report.getEmailCommunications().size()).append("\n");
        }
        if (!report.getProblemEscalations().isEmpty()) {
            sb.append("Problem Escalations: ").append(report.getProblemEscalations().size()).append("\n");
        }
        if (!report.getMeetings().isEmpty()) {
            sb.append("Meetings: ").append(report.getMeetings().size()).append("\n");
        }
        if (!report.getPendingActivities().isEmpty()) {
            sb.append("Pending Activities: ").append(report.getPendingActivities().size()).append("\n");
        }
        
        if (report.getReviewedBy() != null) {
            sb.append("\nReviewed by: ").append(report.getReviewedBy().getUsername());
            if (report.getReviewedAt() != null) {
                sb.append(" on ").append(report.getReviewedAt());
            }
            sb.append("\n");
        }
        if (report.getReviewComments() != null && !report.getReviewComments().isEmpty()) {
            sb.append("Review Comments: ").append(report.getReviewComments()).append("\n");
        }
        
        return sb.toString();
    }
    
    private boolean hasSupervisorAccess(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_DAILY_REPORT_SUPERVISOR") ||
                            role.getName().equals("ROLE_ADMIN"));
    }
}


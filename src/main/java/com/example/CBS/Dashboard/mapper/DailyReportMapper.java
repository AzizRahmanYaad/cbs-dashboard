package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.dailyreport.*;
import com.example.CBS.Dashboard.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DailyReportMapper {
    
    public DailyReportDto toDto(DailyReport report) {
        if (report == null) {
            return null;
        }
        
        DailyReportDto dto = new DailyReportDto();
        dto.setId(report.getId());
        dto.setBusinessDate(report.getBusinessDate());
        dto.setCbsEndTime(report.getCbsEndTime());
        dto.setCbsStartTimeNextDay(report.getCbsStartTimeNextDay());
        dto.setStatus(report.getStatus());
        dto.setReviewComments(report.getReviewComments());
        dto.setReportingLine(report.getReportingLine());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        
        if (report.getEmployee() != null) {
            dto.setEmployeeId(report.getEmployee().getId());
            dto.setEmployeeUsername(report.getEmployee().getUsername());
            dto.setEmployeeFullName(report.getEmployee().getFullName());
            dto.setEmployeeEmail(report.getEmployee().getEmail());
        }
        
        if (report.getReviewedBy() != null) {
            dto.setReviewedById(report.getReviewedBy().getId());
            dto.setReviewedByUsername(report.getReviewedBy().getUsername());
        }
        dto.setReviewedAt(report.getReviewedAt());
        
        // Map collections (handle null safely)
        dto.setChatCommunications(report.getChatCommunications() != null ? 
            report.getChatCommunications().stream().map(this::toChatDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setEmailCommunications(report.getEmailCommunications() != null ? 
            report.getEmailCommunications().stream().map(this::toEmailDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setProblemEscalations(report.getProblemEscalations() != null ? 
            report.getProblemEscalations().stream().map(this::toEscalationDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setTrainingCapacityBuildings(report.getTrainingCapacityBuildings() != null ? 
            report.getTrainingCapacityBuildings().stream().map(this::toTrainingDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setProjectProgressUpdates(report.getProjectProgressUpdates() != null ? 
            report.getProjectProgressUpdates().stream().map(this::toProjectDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setCbsTeamActivities(report.getCbsTeamActivities() != null ? 
            report.getCbsTeamActivities().stream().map(this::toActivityDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setPendingActivities(report.getPendingActivities() != null ? 
            report.getPendingActivities().stream().map(this::toPendingDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setMeetings(report.getMeetings() != null ? 
            report.getMeetings().stream().map(this::toMeetingDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setAfpayCardRequests(report.getAfpayCardRequests() != null ? 
            report.getAfpayCardRequests().stream().map(this::toAfpayDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        dto.setQrmisIssues(report.getQrmisIssues() != null ? 
            report.getQrmisIssues().stream().map(this::toQrmisDto).collect(Collectors.toList()) : 
            new java.util.ArrayList<>());
        
        return dto;
    }
    
    public ChatCommunicationDto toChatDto(ChatCommunication chat) {
        if (chat == null) return null;
        ChatCommunicationDto dto = new ChatCommunicationDto();
        dto.setId(chat.getId());
        dto.setPlatform(chat.getPlatform());
        dto.setSummary(chat.getSummary());
        dto.setActionTaken(chat.getActionTaken());
        dto.setActionPerformed(chat.getActionPerformed());
        dto.setReferenceNumber(chat.getReferenceNumber());
        return dto;
    }
    
    public EmailCommunicationDto toEmailDto(EmailCommunication email) {
        if (email == null) return null;
        EmailCommunicationDto dto = new EmailCommunicationDto();
        dto.setId(email.getId());
        dto.setIsInternal(email.getIsInternal());
        dto.setSender(email.getSender());
        dto.setReceiver(email.getReceiver());
        dto.setSubject(email.getSubject());
        dto.setSummary(email.getSummary());
        dto.setActionTaken(email.getActionTaken());
        dto.setFollowUpRequired(email.getFollowUpRequired());
        return dto;
    }
    
    public ProblemEscalationDto toEscalationDto(ProblemEscalation escalation) {
        if (escalation == null) return null;
        ProblemEscalationDto dto = new ProblemEscalationDto();
        dto.setId(escalation.getId());
        dto.setEscalatedTo(escalation.getEscalatedTo());
        dto.setReason(escalation.getReason());
        dto.setEscalationDateTime(escalation.getEscalationDateTime());
        dto.setFollowUpStatus(escalation.getFollowUpStatus());
        dto.setComments(escalation.getComments());
        return dto;
    }
    
    public TrainingCapacityBuildingDto toTrainingDto(TrainingCapacityBuilding training) {
        if (training == null) return null;
        TrainingCapacityBuildingDto dto = new TrainingCapacityBuildingDto();
        dto.setId(training.getId());
        dto.setTrainingType(training.getTrainingType());
        dto.setTopic(training.getTopic());
        dto.setDuration(training.getDuration());
        dto.setSkillsGained(training.getSkillsGained());
        dto.setTrainerName(training.getTrainerName());
        dto.setParticipants(training.getParticipants());
        return dto;
    }
    
    public ProjectProgressUpdateDto toProjectDto(ProjectProgressUpdate project) {
        if (project == null) return null;
        ProjectProgressUpdateDto dto = new ProjectProgressUpdateDto();
        dto.setId(project.getId());
        dto.setProjectName(project.getProjectName());
        dto.setTaskOrMilestone(project.getTaskOrMilestone());
        dto.setProgressDetail(project.getProgressDetail());
        dto.setRoadblocksIssues(project.getRoadblocksIssues());
        dto.setEstimatedCompletionDate(project.getEstimatedCompletionDate());
        dto.setComments(project.getComments());
        return dto;
    }
    
    public CbsTeamActivityDto toActivityDto(CbsTeamActivity activity) {
        if (activity == null) return null;
        CbsTeamActivityDto dto = new CbsTeamActivityDto();
        dto.setId(activity.getId());
        dto.setDescription(activity.getDescription());
        dto.setBranch(activity.getBranch());
        dto.setAccountNumber(activity.getAccountNumber());
        dto.setActionTaken(activity.getActionTaken());
        dto.setFinalStatus(activity.getFinalStatus());
        dto.setActivityType(activity.getActivityType());
        return dto;
    }
    
    public PendingActivityDto toPendingDto(PendingActivity pending) {
        if (pending == null) return null;
        PendingActivityDto dto = new PendingActivityDto();
        dto.setId(pending.getId());
        dto.setTitle(pending.getTitle());
        dto.setDescription(pending.getDescription());
        dto.setStatus(pending.getStatus());
        dto.setAmount(pending.getAmount());
        dto.setFollowUpRequired(pending.getFollowUpRequired());
        dto.setResponsiblePerson(pending.getResponsiblePerson());
        return dto;
    }
    
    public MeetingDto toMeetingDto(Meeting meeting) {
        if (meeting == null) return null;
        MeetingDto dto = new MeetingDto();
        dto.setId(meeting.getId());
        dto.setMeetingType(meeting.getMeetingType());
        dto.setTopic(meeting.getTopic());
        dto.setSummary(meeting.getSummary());
        dto.setActionTaken(meeting.getActionTaken());
        dto.setNextStep(meeting.getNextStep());
        dto.setParticipants(meeting.getParticipants());
        return dto;
    }
    
    public AfpayCardRequestDto toAfpayDto(AfpayCardRequest afpay) {
        if (afpay == null) return null;
        AfpayCardRequestDto dto = new AfpayCardRequestDto();
        dto.setId(afpay.getId());
        dto.setRequestType(afpay.getRequestType());
        dto.setRequestedBy(afpay.getRequestedBy());
        dto.setRequestDate(afpay.getRequestDate());
        dto.setResolutionDetails(afpay.getResolutionDetails());
        dto.setSupportingDocumentPath(afpay.getSupportingDocumentPath());
        dto.setArchivedDate(afpay.getArchivedDate());
        dto.setOperator(afpay.getOperator());
        return dto;
    }
    
    public QrmisIssueDto toQrmisDto(QrmisIssue qrmis) {
        if (qrmis == null) return null;
        QrmisIssueDto dto = new QrmisIssueDto();
        dto.setId(qrmis.getId());
        dto.setProblemType(qrmis.getProblemType());
        dto.setProblemDescription(qrmis.getProblemDescription());
        dto.setSolutionProvided(qrmis.getSolutionProvided());
        dto.setPostedBy(qrmis.getPostedBy());
        dto.setAuthorizedBy(qrmis.getAuthorizedBy());
        dto.setSupportingDocumentsArchived(qrmis.getSupportingDocumentsArchived());
        dto.setOperator(qrmis.getOperator());
        return dto;
    }
    
    public void updateEntityFromDto(DailyReport report, UpdateDailyReportRequest request) {
        if (request.getCbsEndTime() != null) {
            report.setCbsEndTime(request.getCbsEndTime());
        }
        if (request.getCbsStartTimeNextDay() != null) {
            report.setCbsStartTimeNextDay(request.getCbsStartTimeNextDay());
        }
        if (request.getReportingLine() != null) {
            report.setReportingLine(request.getReportingLine());
        }
        
        // Update collections - clear and add new
        report.getChatCommunications().clear();
        if (request.getChatCommunications() != null) {
            request.getChatCommunications().forEach(dto -> {
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
        
        // Similar updates for other collections...
        updateEmailCommunications(report, request);
        updateProblemEscalations(report, request);
        updateTrainingCapacityBuildings(report, request);
        updateProjectProgressUpdates(report, request);
        updateCbsTeamActivities(report, request);
        updatePendingActivities(report, request);
        updateMeetings(report, request);
        updateAfpayCardRequests(report, request);
        updateQrmisIssues(report, request);
    }
    
    private void updateEmailCommunications(DailyReport report, UpdateDailyReportRequest request) {
        report.getEmailCommunications().clear();
        if (request.getEmailCommunications() != null) {
            request.getEmailCommunications().forEach(dto -> {
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
    
    private void updateProblemEscalations(DailyReport report, UpdateDailyReportRequest request) {
        report.getProblemEscalations().clear();
        if (request.getProblemEscalations() != null) {
            request.getProblemEscalations().forEach(dto -> {
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
    
    private void updateTrainingCapacityBuildings(DailyReport report, UpdateDailyReportRequest request) {
        report.getTrainingCapacityBuildings().clear();
        if (request.getTrainingCapacityBuildings() != null) {
            request.getTrainingCapacityBuildings().forEach(dto -> {
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
    
    private void updateProjectProgressUpdates(DailyReport report, UpdateDailyReportRequest request) {
        report.getProjectProgressUpdates().clear();
        if (request.getProjectProgressUpdates() != null) {
            request.getProjectProgressUpdates().forEach(dto -> {
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
    
    private void updateCbsTeamActivities(DailyReport report, UpdateDailyReportRequest request) {
        report.getCbsTeamActivities().clear();
        if (request.getCbsTeamActivities() != null) {
            request.getCbsTeamActivities().forEach(dto -> {
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
    
    private void updatePendingActivities(DailyReport report, UpdateDailyReportRequest request) {
        report.getPendingActivities().clear();
        if (request.getPendingActivities() != null) {
            request.getPendingActivities().forEach(dto -> {
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
    
    private void updateMeetings(DailyReport report, UpdateDailyReportRequest request) {
        report.getMeetings().clear();
        if (request.getMeetings() != null) {
            request.getMeetings().forEach(dto -> {
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
    
    private void updateAfpayCardRequests(DailyReport report, UpdateDailyReportRequest request) {
        report.getAfpayCardRequests().clear();
        if (request.getAfpayCardRequests() != null) {
            request.getAfpayCardRequests().forEach(dto -> {
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
    
    private void updateQrmisIssues(DailyReport report, UpdateDailyReportRequest request) {
        report.getQrmisIssues().clear();
        if (request.getQrmisIssues() != null) {
            request.getQrmisIssues().forEach(dto -> {
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
}


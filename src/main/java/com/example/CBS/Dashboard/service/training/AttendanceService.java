package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.AttendanceDto;
import com.example.CBS.Dashboard.dto.training.MarkAttendanceRequest;
import com.example.CBS.Dashboard.entity.Attendance;
import com.example.CBS.Dashboard.entity.TrainingSession;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.AttendanceRepository;
import com.example.CBS.Dashboard.repository.TrainingSessionRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final TrainingSessionRepository sessionRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void markAttendance(MarkAttendanceRequest request, String username) {
        User markedBy = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TrainingSession session = sessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new RuntimeException("Training session not found"));
        
        for (MarkAttendanceRequest.StudentAttendance studentAttendance : request.getAttendances()) {
            User participant = userRepository.findById(studentAttendance.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));
            
            // Check if attendance already exists
            attendanceRepository.findBySessionIdAndParticipantId(
                request.getSessionId(), 
                studentAttendance.getParticipantId()
            ).ifPresentOrElse(
                existing -> {
                    // Update existing attendance
                    existing.setStatus(Attendance.AttendanceStatus.valueOf(studentAttendance.getStatus()));
                    existing.setNotes(studentAttendance.getNotes());
                    existing.setAttendanceDate(studentAttendance.getAttendanceDate() != null ? 
                        studentAttendance.getAttendanceDate() : LocalDateTime.now());
                    existing.setMarkedBy(markedBy);
                    attendanceRepository.save(existing);
                },
                () -> {
                    // Create new attendance
                    Attendance attendance = new Attendance();
                    attendance.setSession(session);
                    attendance.setParticipant(participant);
                    attendance.setStatus(Attendance.AttendanceStatus.valueOf(studentAttendance.getStatus()));
                    attendance.setNotes(studentAttendance.getNotes());
                    attendance.setAttendanceDate(studentAttendance.getAttendanceDate() != null ? 
                        studentAttendance.getAttendanceDate() : LocalDateTime.now());
                    attendance.setMarkedBy(markedBy);
                    attendanceRepository.save(attendance);
                }
            );
        }
    }
    
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceBySession(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceByParticipant(Long participantId) {
        return attendanceRepository.findByParticipantId(participantId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    private AttendanceDto mapToDto(Attendance attendance) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(attendance.getId());
        dto.setSessionId(attendance.getSession().getId());
        dto.setSessionTitle(attendance.getSession().getProgram().getTitle() + " - " + 
            attendance.getSession().getStartDateTime().toString());
        dto.setParticipantId(attendance.getParticipant().getId());
        dto.setParticipantUsername(attendance.getParticipant().getUsername());
        dto.setParticipantFullName(attendance.getParticipant().getFullName());
        dto.setParticipantEmail(attendance.getParticipant().getEmail());
        dto.setStatus(attendance.getStatus().name());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setNotes(attendance.getNotes());
        dto.setMarkedById(attendance.getMarkedBy().getId());
        dto.setMarkedByUsername(attendance.getMarkedBy().getUsername());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());
        return dto;
    }
}

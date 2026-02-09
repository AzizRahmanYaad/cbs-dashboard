package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.AttendanceDto;
import com.example.CBS.Dashboard.dto.training.MarkAttendanceRequest;
import com.example.CBS.Dashboard.entity.Attendance;
import com.example.CBS.Dashboard.entity.TrainingSession;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.AttendanceRepository;
import com.example.CBS.Dashboard.repository.TrainingSessionRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import com.example.CBS.Dashboard.service.user.UserService;
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
    private final UserService userService;

    @Transactional
    public void markAttendance(MarkAttendanceRequest request, String username) {
        User markedBy = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        TrainingSession session = sessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new RuntimeException("Training session not found"));

        for (MarkAttendanceRequest.StudentAttendance studentAttendance : request.getAttendances()) {
            User participant = userRepository.findById(studentAttendance.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

            Attendance.AttendanceStatus status = Attendance.AttendanceStatus.valueOf(studentAttendance.getStatus());
            LocalDateTime now = studentAttendance.getAttendanceDate() != null
                ? studentAttendance.getAttendanceDate() : LocalDateTime.now();

            final String signatureData;
            final Attendance.SignatureType signatureType;
            final LocalDateTime signedAt;
            if (status == Attendance.AttendanceStatus.PRESENT) {
                String participantSignature = userService.getSignatureData(participant.getUsername());
                if (participantSignature == null || participantSignature.isBlank()) {
                    throw new IllegalStateException(
                        "Cannot mark Present: student '" + (participant.getFullName() != null ? participant.getFullName() : participant.getUsername())
                            + "' has no saved e-signature. Please ask the student to create and save their signature in My E-Signature first.");
                }
                signatureData = participantSignature;
                signatureType = Attendance.SignatureType.PRESENT;
                signedAt = LocalDateTime.now();
            } else {
                signatureData = null;
                signatureType = null;
                signedAt = null;
            }

            attendanceRepository.findBySessionIdAndParticipantId(
                request.getSessionId(),
                studentAttendance.getParticipantId()
            ).ifPresentOrElse(
                existing -> {
                    existing.setStatus(status);
                    existing.setNotes(studentAttendance.getNotes());
                    existing.setAttendanceDate(now);
                    existing.setMarkedBy(markedBy);
                    if (signatureData != null) {
                        existing.setSignatureData(signatureData);
                        existing.setSignatureType(signatureType);
                        existing.setSignedAt(signedAt);
                    }
                    attendanceRepository.save(existing);
                },
                () -> {
                    Attendance attendance = new Attendance();
                    attendance.setSession(session);
                    attendance.setParticipant(participant);
                    attendance.setStatus(status);
                    attendance.setNotes(studentAttendance.getNotes());
                    attendance.setAttendanceDate(now);
                    attendance.setMarkedBy(markedBy);
                    if (signatureData != null) {
                        attendance.setSignatureData(signatureData);
                        attendance.setSignatureType(signatureType);
                        attendance.setSignedAt(signedAt);
                    }
                    attendanceRepository.save(attendance);
                }
            );
        }
    }

    /** Student acknowledges session materials for an ABSENT/EXCUSED record by applying their e-signature. */
    @Transactional
    public void acknowledgeAttendance(Long attendanceId, String signatureData, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        if (!attendance.getParticipant().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only acknowledge your own attendance record.");
        }
        if (attendance.getStatus() != Attendance.AttendanceStatus.ABSENT && attendance.getStatus() != Attendance.AttendanceStatus.EXCUSED) {
            throw new IllegalArgumentException("Only ABSENT or EXCUSED records can be acknowledged.");
        }
        if (signatureData == null || signatureData.isBlank()) {
            throw new IllegalArgumentException("Signature is required to acknowledge.");
        }
        String cleanData = signatureData.contains(",") ? signatureData.substring(signatureData.indexOf(",") + 1).trim() : signatureData;
        attendance.setSignatureData(cleanData);
        attendance.setSignatureType(Attendance.SignatureType.ACKNOWLEDGMENT);
        attendance.setSignedAt(LocalDateTime.now());
        attendanceRepository.save(attendance);
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
        dto.setSignatureData(attendance.getSignatureData());
        dto.setSignatureType(attendance.getSignatureType() != null ? attendance.getSignatureType().name() : null);
        dto.setSignedAt(attendance.getSignedAt());
        return dto;
    }
}

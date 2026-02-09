package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.AcknowledgeAttendanceRequest;
import com.example.CBS.Dashboard.dto.training.AttendanceDto;
import com.example.CBS.Dashboard.dto.training.MarkAttendanceRequest;
import com.example.CBS.Dashboard.service.training.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        String username = authentication.getName();
        attendanceService.markAttendance(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceBySession(@PathVariable Long sessionId) {
        List<AttendanceDto> attendances = attendanceService.getAttendanceBySession(sessionId);
        return ResponseEntity.ok(attendances);
    }
    
    @GetMapping("/participant/{participantId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByParticipant(@PathVariable Long participantId) {
        List<AttendanceDto> attendances = attendanceService.getAttendanceByParticipant(participantId);
        return ResponseEntity.ok(attendances);
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> acknowledgeAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AcknowledgeAttendanceRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        attendanceService.acknowledgeAttendance(id, request.getSignatureData(), authentication.getName());
        return ResponseEntity.ok().build();
    }
}

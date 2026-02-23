package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.CreateTrainingSessionRequest;
import com.example.CBS.Dashboard.dto.training.TrainingSessionDto;
import com.example.CBS.Dashboard.service.training.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/sessions")
@RequiredArgsConstructor
public class TrainingSessionController {
    
    private final TrainingSessionService sessionService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingSessionDto> createSession(
            @Valid @RequestBody CreateTrainingSessionRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        String username = authentication.getName();
        TrainingSessionDto session = sessionService.createSession(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingSessionDto>> getAllSessions(
            @RequestParam(required = false) Long programId,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<TrainingSessionDto> sessions =
                sessionService.getSessionsForUser(authentication.getName(), programId);
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrainingSessionDto> getSessionById(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TrainingSessionDto session = sessionService.getSessionById(id, authentication.getName());
        return ResponseEntity.ok(session);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingSessionDto> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingSessionRequest request) {
        TrainingSessionDto session = sessionService.updateSession(id, request);
        return ResponseEntity.ok(session);
    }

    /**
     * Permanently deletes all training sessions.
     * Restricted to training admins and system admins.
     */
    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAllSessions() {
        sessionService.deleteAllSessions();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}

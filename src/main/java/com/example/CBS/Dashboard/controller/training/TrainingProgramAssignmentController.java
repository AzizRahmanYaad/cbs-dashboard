package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.AssignStudentRequest;
import com.example.CBS.Dashboard.dto.training.AssignTeacherRequest;
import com.example.CBS.Dashboard.dto.training.TrainingProgramDto;
import com.example.CBS.Dashboard.service.training.TrainingProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training/programs/assignments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TrainingProgramAssignmentController {
    
    private final TrainingProgramService trainingProgramService;
    
    @PostMapping("/teacher")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingProgramDto> assignTeacher(
            @Valid @RequestBody AssignTeacherRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        String username = authentication.getName();
        TrainingProgramDto program = trainingProgramService.assignTeacher(request, username);
        return ResponseEntity.ok(program);
    }
    
    @PostMapping("/students")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> assignStudents(
            @Valid @RequestBody AssignStudentRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        String username = authentication.getName();
        trainingProgramService.assignStudents(request, username);
        return ResponseEntity.ok().build();
    }
}

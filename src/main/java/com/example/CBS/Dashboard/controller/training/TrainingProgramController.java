package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.CreateTrainingProgramRequest;
import com.example.CBS.Dashboard.dto.training.TrainingProgramDto;
import com.example.CBS.Dashboard.service.training.TrainingProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/programs")
@RequiredArgsConstructor
public class TrainingProgramController {
    
    private final TrainingProgramService trainingProgramService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingProgramDto> createProgram(
            @Valid @RequestBody CreateTrainingProgramRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        String username = authentication.getName();
        TrainingProgramDto program = trainingProgramService.createProgram(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(program);
    }
    
    @GetMapping
    public ResponseEntity<List<TrainingProgramDto>> getAllPrograms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        try {
            System.out.println("GET /api/training/programs called with status=" + status + ", category=" + category);
            
            List<TrainingProgramDto> programs;
            // Check if status is provided and not empty
            if (status != null && !status.trim().isEmpty()) {
                programs = trainingProgramService.getProgramsByStatus(status);
            } 
            // Check if category is provided and not empty
            else if (category != null && !category.trim().isEmpty()) {
                programs = trainingProgramService.getProgramsByCategory(category);
            } 
            // Otherwise get all programs
            else {
                programs = trainingProgramService.getAllPrograms();
            }
            
            // Ensure we never return null
            if (programs == null) {
                System.out.println("Warning: getAllPrograms returned null, returning empty list");
                programs = new java.util.ArrayList<>();
            }
            
            System.out.println("Returning " + programs.size() + " programs");
            return ResponseEntity.ok(programs);
        } catch (Exception e) {
            System.err.println("Error in getAllPrograms endpoint:");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + (e.getMessage() != null ? e.getMessage() : "null"));
            e.printStackTrace();
            // Ensure exception has a message
            if (e.getMessage() == null || e.getMessage().trim().isEmpty()) {
                throw new RuntimeException("Failed to retrieve programs: " + e.getClass().getSimpleName(), e);
            }
            // Let GlobalExceptionHandler handle it
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TrainingProgramDto> getProgramById(@PathVariable Long id) {
        TrainingProgramDto program = trainingProgramService.getProgramById(id);
        return ResponseEntity.ok(program);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingProgramDto> updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingProgramRequest request) {
        TrainingProgramDto program = trainingProgramService.updateProgram(id, request);
        return ResponseEntity.ok(program);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        try {
            System.out.println("DELETE /api/training/programs/" + id + " called");
            trainingProgramService.deleteProgram(id);
            System.out.println("Program " + id + " deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("Error deleting program " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @DeleteMapping("/{programId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> removeStudentFromProgram(
            @PathVariable Long programId,
            @PathVariable Long studentId) {
        trainingProgramService.removeStudentFromProgram(programId, studentId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<TrainingProgramDto>> getProgramsByInstructor(@PathVariable Long instructorId) {
        List<TrainingProgramDto> programs = trainingProgramService.getProgramsByInstructor(instructorId);
        return ResponseEntity.ok(programs);
    }
    
    @GetMapping("/{programId}/students")
    public ResponseEntity<List<com.example.CBS.Dashboard.dto.training.EnrollmentDto>> getProgramStudents(
            @PathVariable Long programId) {
        List<com.example.CBS.Dashboard.dto.training.EnrollmentDto> enrollments = 
            trainingProgramService.getProgramEnrollments(programId);
        return ResponseEntity.ok(enrollments);
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TrainingProgramDto>> getProgramsByStudent(@PathVariable Long studentId) {
        List<TrainingProgramDto> programs = trainingProgramService.getProgramsByStudent(studentId);
        return ResponseEntity.ok(programs);
    }
}

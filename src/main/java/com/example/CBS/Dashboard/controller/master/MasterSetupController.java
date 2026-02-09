package com.example.CBS.Dashboard.controller.master;

import com.example.CBS.Dashboard.dto.master.*;
import com.example.CBS.Dashboard.service.master.MasterSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterSetupController {
    
    private final MasterSetupService masterSetupService;
    
    // Training Topic endpoints
    @PostMapping("/training-topics")
    public ResponseEntity<TrainingTopicDto> createTrainingTopic(
            @Valid @RequestBody CreateTrainingTopicRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TrainingTopicDto dto = masterSetupService.createTrainingTopic(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/training-topics")
    public ResponseEntity<List<TrainingTopicDto>> getAllTrainingTopics(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<TrainingTopicDto> topics = masterSetupService.getAllTrainingTopics(activeOnly);
        return ResponseEntity.ok(topics);
    }
    
    @PutMapping("/training-topics/{id}")
    public ResponseEntity<TrainingTopicDto> updateTrainingTopic(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingTopicRequest request) {
        TrainingTopicDto dto = masterSetupService.updateTrainingTopic(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/training-topics/{id}")
    public ResponseEntity<Void> deleteTrainingTopic(@PathVariable Long id) {
        masterSetupService.deleteTrainingTopic(id);
        return ResponseEntity.noContent().build();
    }
    
    // Training Name endpoints
    @PostMapping("/training-names")
    public ResponseEntity<TrainingNameDto> createTrainingName(
            @Valid @RequestBody CreateTrainingNameRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TrainingNameDto dto = masterSetupService.createTrainingName(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/training-names")
    public ResponseEntity<List<TrainingNameDto>> getAllTrainingNames(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<TrainingNameDto> names = masterSetupService.getAllTrainingNames(activeOnly);
        return ResponseEntity.ok(names);
    }
    
    @PutMapping("/training-names/{id}")
    public ResponseEntity<TrainingNameDto> updateTrainingName(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingNameRequest request) {
        TrainingNameDto dto = masterSetupService.updateTrainingName(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/training-names/{id}")
    public ResponseEntity<Void> deleteTrainingName(@PathVariable Long id) {
        masterSetupService.deleteTrainingName(id);
        return ResponseEntity.noContent().build();
    }
    
    // Training Module endpoints
    @PostMapping("/training-modules")
    public ResponseEntity<TrainingModuleDto> createTrainingModule(
            @Valid @RequestBody CreateTrainingModuleRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TrainingModuleDto dto = masterSetupService.createTrainingModule(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/training-modules")
    public ResponseEntity<List<TrainingModuleDto>> getAllTrainingModules(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<TrainingModuleDto> modules = masterSetupService.getAllTrainingModules(activeOnly);
        return ResponseEntity.ok(modules);
    }
    
    @PutMapping("/training-modules/{id}")
    public ResponseEntity<TrainingModuleDto> updateTrainingModule(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingModuleRequest request) {
        TrainingModuleDto dto = masterSetupService.updateTrainingModule(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/training-modules/{id}")
    public ResponseEntity<Void> deleteTrainingModule(@PathVariable Long id) {
        masterSetupService.deleteTrainingModule(id);
        return ResponseEntity.noContent().build();
    }
    
    // Training Category endpoints
    @PostMapping("/training-categories")
    public ResponseEntity<TrainingCategoryMasterDto> createTrainingCategory(
            @Valid @RequestBody CreateTrainingCategoryMasterRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TrainingCategoryMasterDto dto = masterSetupService.createTrainingCategory(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/training-categories")
    public ResponseEntity<List<TrainingCategoryMasterDto>> getAllTrainingCategories(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<TrainingCategoryMasterDto> categories = masterSetupService.getAllTrainingCategories(activeOnly);
        return ResponseEntity.ok(categories);
    }
    
    @PutMapping("/training-categories/{id}")
    public ResponseEntity<TrainingCategoryMasterDto> updateTrainingCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingCategoryMasterRequest request) {
        TrainingCategoryMasterDto dto = masterSetupService.updateTrainingCategory(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/training-categories/{id}")
    public ResponseEntity<Void> deleteTrainingCategory(@PathVariable Long id) {
        masterSetupService.deleteTrainingCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // Department endpoints
    @PostMapping("/departments")
    public ResponseEntity<DepartmentDto> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        DepartmentDto dto = masterSetupService.createDepartment(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<DepartmentDto> departments = masterSetupService.getAllDepartments(activeOnly);
        return ResponseEntity.ok(departments);
    }
    
    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDto dto = masterSetupService.updateDepartment(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        masterSetupService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
    
    // Coordinator endpoints
    @PostMapping("/coordinators")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<CoordinatorDto> createCoordinator(
            @Valid @RequestBody CreateCoordinatorRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        CoordinatorDto dto = masterSetupService.createCoordinator(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/coordinators")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_TRAINING')")
    public ResponseEntity<List<CoordinatorDto>> getAllCoordinators(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<CoordinatorDto> coordinators = masterSetupService.getAllCoordinators(activeOnly);
        return ResponseEntity.ok(coordinators);
    }
    
    @PutMapping("/coordinators/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<CoordinatorDto> updateCoordinator(
            @PathVariable Long id,
            @Valid @RequestBody CreateCoordinatorRequest request) {
        CoordinatorDto dto = masterSetupService.updateCoordinator(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/coordinators/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCoordinator(@PathVariable Long id) {
        masterSetupService.deleteCoordinator(id);
        return ResponseEntity.noContent().build();
    }
    
    // Student/Teacher endpoints
    @PostMapping("/student-teachers")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<StudentTeacherDto> createStudentTeacher(
            @Valid @RequestBody CreateStudentTeacherRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        StudentTeacherDto dto = masterSetupService.createStudentTeacher(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @GetMapping("/student-teachers")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_TRAINING')")
    public ResponseEntity<List<StudentTeacherDto>> getAllStudentTeachers(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) String type) {
        List<StudentTeacherDto> studentTeachers = masterSetupService.getAllStudentTeachers(activeOnly, type);
        return ResponseEntity.ok(studentTeachers);
    }
    
    @PutMapping("/student-teachers/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<StudentTeacherDto> updateStudentTeacher(
            @PathVariable Long id,
            @Valid @RequestBody CreateStudentTeacherRequest request) {
        StudentTeacherDto dto = masterSetupService.updateStudentTeacher(id, request);
        return ResponseEntity.ok(dto);
    }
    
    @DeleteMapping("/student-teachers/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteStudentTeacher(@PathVariable Long id) {
        masterSetupService.deleteStudentTeacher(id);
        return ResponseEntity.noContent().build();
    }
}

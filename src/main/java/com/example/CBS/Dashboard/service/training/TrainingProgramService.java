package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.AssignStudentRequest;
import com.example.CBS.Dashboard.dto.training.AssignTeacherRequest;
import com.example.CBS.Dashboard.dto.training.CreateTrainingProgramRequest;
import com.example.CBS.Dashboard.dto.training.TrainingProgramDto;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.mapper.TrainingProgramMapper;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingProgramService {
    
    private final TrainingProgramRepository trainingProgramRepository;
    private final UserRepository userRepository;
    private final TrainingProgramMapper mapper;
    private final TrainingTopicRepository trainingTopicRepository;
    private final TrainingNameRepository trainingNameRepository;
    private final TrainingCategoryMasterRepository trainingCategoryMasterRepository;
    private final DepartmentRepository departmentRepository;
    private final TrainingModuleRepository trainingModuleRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final StudentTeacherRepository studentTeacherRepository;
    
    @Transactional
    public TrainingProgramDto createProgram(CreateTrainingProgramRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TrainingProgram program = new TrainingProgram();
        
        // Validate and set title (required field)
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required and cannot be empty");
        }
        program.setTitle(request.getTitle().trim());
        program.setDescription(request.getDescription());
        
        // New fields
        if (request.getTrainingTopicId() != null) {
            TrainingTopic topic = trainingTopicRepository.findById(request.getTrainingTopicId())
                .orElseThrow(() -> new RuntimeException("Training topic not found"));
            program.setTrainingTopic(topic);
        }
        if (request.getTrainingNameId() != null) {
            TrainingName trainingName = trainingNameRepository.findById(request.getTrainingNameId())
                .orElseThrow(() -> new RuntimeException("Training name not found"));
            program.setTrainingName(trainingName);
        }
        if (request.getTrainingName() != null) {
            program.setTrainingNameString(request.getTrainingName());
        }
        program.setTrainingDate(request.getTrainingDate());
        if (request.getTrainingLevel() != null && !request.getTrainingLevel().trim().isEmpty()) {
            try {
                // Handle enum values that might come with prefix (e.g., "TrainingLevel.BASIC" -> "BASIC")
                String levelStr = request.getTrainingLevel().trim();
                if (levelStr.contains(".")) {
                    levelStr = levelStr.substring(levelStr.lastIndexOf(".") + 1);
                }
                program.setTrainingLevel(TrainingProgram.TrainingLevel.valueOf(levelStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid training level: " + request.getTrainingLevel() + ". Valid values are: BASIC, INTERMEDIATE, ADVANCED");
            }
        }
        if (request.getTrainingCategoryId() != null) {
            TrainingCategoryMaster category = trainingCategoryMasterRepository.findById(request.getTrainingCategoryId())
                .orElseThrow(() -> new RuntimeException("Training category not found"));
            program.setTrainingCategory(category);
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            program.setDepartment(department);
        }
        if (request.getTrainingModuleId() != null) {
            TrainingModule module = trainingModuleRepository.findById(request.getTrainingModuleId())
                .orElseThrow(() -> new RuntimeException("Training module not found"));
            program.setTrainingModule(module);
        }
        program.setFacultyName(request.getFacultyName());
        if (request.getCoordinatorId() != null) {
            Coordinator coordinator = coordinatorRepository.findById(request.getCoordinatorId())
                .orElseThrow(() -> new RuntimeException("Coordinator not found"));
            program.setCoordinator(coordinator);
        }
        if (request.getTrainingType() != null && !request.getTrainingType().trim().isEmpty()) {
            try {
                // Handle enum values that might come with prefix
                String typeStr = request.getTrainingType().trim();
                if (typeStr.contains(".")) {
                    typeStr = typeStr.substring(typeStr.lastIndexOf(".") + 1);
                }
                program.setTrainingType(TrainingProgram.TrainingType.valueOf(typeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid training type: " + request.getTrainingType() + ". Valid values are: ON_SITE, ONLINE, ON_JOB");
            }
        }
        if (request.getExamType() != null && !request.getExamType().trim().isEmpty()) {
            try {
                // Handle enum values that might come with prefix
                String examStr = request.getExamType().trim();
                if (examStr.contains(".")) {
                    examStr = examStr.substring(examStr.lastIndexOf(".") + 1);
                }
                program.setExamType(TrainingProgram.ExamType.valueOf(examStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid exam type: " + request.getExamType() + ". Valid values are: PRE_TRAINING_EXAM, POST_TRAINING_EXAM");
            }
        }
        program.setHasArticleMaterial(request.getHasArticleMaterial() != null ? request.getHasArticleMaterial() : false);
        program.setHasVideoMaterial(request.getHasVideoMaterial() != null ? request.getHasVideoMaterial() : false);
        program.setHasSlideMaterial(request.getHasSlideMaterial() != null ? request.getHasSlideMaterial() : false);
        program.setThumbnailImagePath(request.getThumbnailImagePath());
        
        // Legacy fields
        program.setCategory(request.getCategory());
        program.setDurationHours(request.getDurationHours());
        program.setStatus(request.getStatus() != null ? 
            TrainingProgram.TrainingStatus.valueOf(request.getStatus()) : 
            TrainingProgram.TrainingStatus.DRAFT);
        program.setMaxParticipants(request.getMaxParticipants());
        program.setPrerequisites(request.getPrerequisites());
        program.setLearningObjectives(request.getLearningObjectives());
        program.setCreatedBy(user);
        
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
            program.setInstructor(instructor);
        }
        
        TrainingProgram saved = trainingProgramRepository.save(program);
        
        // Initialize only basic relationships needed for DTO (skip sessions/enrollments to avoid issues)
        initializeBasicRelationships(saved);
        
        return mapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingProgramDto> getAllPrograms() {
        try {
            System.out.println("TrainingProgramService.getAllPrograms() called");
            // Newest programs first (descending by createdAt)
            List<TrainingProgram> programs = trainingProgramRepository.findAllByOrderByCreatedAtDesc();
            System.out.println("Found " + (programs != null ? programs.size() : 0) + " programs in database");
            
            if (programs == null || programs.isEmpty()) {
                System.out.println("No programs found, returning empty list");
                return new java.util.ArrayList<>();
            }
            
            List<TrainingProgramDto> dtos = new java.util.ArrayList<>();
            for (TrainingProgram program : programs) {
                try {
                    System.out.println("Processing program ID: " + program.getId() + ", Title: " + program.getTitle());
                    // Initialize relationships for each program
                    initializeRelationships(program);
                    // Map to DTO
                    TrainingProgramDto dto = mapper.toDto(program);
                    // Only add non-null DTOs
                    if (dto != null) {
                        dtos.add(dto);
                        System.out.println("Successfully mapped program ID: " + dto.getId());
                    } else {
                        System.out.println("Warning: Mapper returned null for program ID: " + program.getId());
                    }
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = e.getClass().getSimpleName();
                    }
                    System.err.println("Error mapping program with ID " + program.getId() + ": " + errorMsg);
                    e.printStackTrace();
                    // Continue with other programs instead of failing completely
                }
            }
            System.out.println("Returning " + dtos.size() + " program DTOs");
            return dtos;
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMsg += ": " + e.getCause().getMessage();
                }
            }
            System.err.println("Error in getAllPrograms: " + errorMsg);
            System.err.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve programs: " + errorMsg, e);
        }
    }
    
    @Transactional(readOnly = true)
    public TrainingProgramDto getProgramById(Long id) {
        TrainingProgram program = trainingProgramRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training program not found with id: " + id));
        initializeRelationships(program);
        return mapper.toDto(program);
    }
    
    @Transactional
    public TrainingProgramDto updateProgram(Long id, CreateTrainingProgramRequest request) {
        TrainingProgram program = trainingProgramRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training program not found with id: " + id));
        
        // Validate and set title (required field)
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required and cannot be empty");
        }
        program.setTitle(request.getTitle().trim());
        program.setDescription(request.getDescription());
        
        // Update new fields
        if (request.getTrainingTopicId() != null) {
            TrainingTopic topic = trainingTopicRepository.findById(request.getTrainingTopicId())
                .orElseThrow(() -> new RuntimeException("Training topic not found"));
            program.setTrainingTopic(topic);
        } else {
            program.setTrainingTopic(null);
        }
        if (request.getTrainingNameId() != null) {
            TrainingName trainingName = trainingNameRepository.findById(request.getTrainingNameId())
                .orElseThrow(() -> new RuntimeException("Training name not found"));
            program.setTrainingName(trainingName);
        } else {
            program.setTrainingName(null);
        }
        if (request.getTrainingName() != null) {
            program.setTrainingNameString(request.getTrainingName());
        } else {
            program.setTrainingNameString(null);
        }
        program.setTrainingDate(request.getTrainingDate());
        if (request.getTrainingLevel() != null && !request.getTrainingLevel().trim().isEmpty()) {
            try {
                // Handle enum values that might come with prefix (e.g., "TrainingLevel.BASIC" -> "BASIC")
                String levelStr = request.getTrainingLevel().trim();
                if (levelStr.contains(".")) {
                    levelStr = levelStr.substring(levelStr.lastIndexOf(".") + 1);
                }
                program.setTrainingLevel(TrainingProgram.TrainingLevel.valueOf(levelStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid training level: " + request.getTrainingLevel() + ". Valid values are: BASIC, INTERMEDIATE, ADVANCED");
            }
        }
        if (request.getTrainingCategoryId() != null) {
            TrainingCategoryMaster category = trainingCategoryMasterRepository.findById(request.getTrainingCategoryId())
                .orElseThrow(() -> new RuntimeException("Training category not found"));
            program.setTrainingCategory(category);
        } else {
            program.setTrainingCategory(null);
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            program.setDepartment(department);
        } else {
            program.setDepartment(null);
        }
        if (request.getTrainingModuleId() != null) {
            TrainingModule module = trainingModuleRepository.findById(request.getTrainingModuleId())
                .orElseThrow(() -> new RuntimeException("Training module not found"));
            program.setTrainingModule(module);
        } else {
            program.setTrainingModule(null);
        }
        program.setFacultyName(request.getFacultyName());
        if (request.getCoordinatorId() != null) {
            Coordinator coordinator = coordinatorRepository.findById(request.getCoordinatorId())
                .orElseThrow(() -> new RuntimeException("Coordinator not found"));
            program.setCoordinator(coordinator);
        } else {
            program.setCoordinator(null);
        }
        if (request.getTrainingType() != null && !request.getTrainingType().trim().isEmpty()) {
            try {
                // Handle enum values that might come with prefix
                String typeStr = request.getTrainingType().trim();
                if (typeStr.contains(".")) {
                    typeStr = typeStr.substring(typeStr.lastIndexOf(".") + 1);
                }
                program.setTrainingType(TrainingProgram.TrainingType.valueOf(typeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid training type: " + request.getTrainingType() + ". Valid values are: ON_SITE, ONLINE, ON_JOB");
            }
        }
        if (request.getExamType() != null && !request.getExamType().trim().isEmpty()) {
            try {
                // Handle enum values that might come with prefix
                String examStr = request.getExamType().trim();
                if (examStr.contains(".")) {
                    examStr = examStr.substring(examStr.lastIndexOf(".") + 1);
                }
                program.setExamType(TrainingProgram.ExamType.valueOf(examStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid exam type: " + request.getExamType() + ". Valid values are: PRE_TRAINING_EXAM, POST_TRAINING_EXAM");
            }
        }
        if (request.getHasArticleMaterial() != null) {
            program.setHasArticleMaterial(request.getHasArticleMaterial());
        }
        if (request.getHasVideoMaterial() != null) {
            program.setHasVideoMaterial(request.getHasVideoMaterial());
        }
        if (request.getHasSlideMaterial() != null) {
            program.setHasSlideMaterial(request.getHasSlideMaterial());
        }
        program.setThumbnailImagePath(request.getThumbnailImagePath());
        
        // Update legacy fields
        program.setCategory(request.getCategory());
        program.setDurationHours(request.getDurationHours());
        if (request.getStatus() != null) {
            program.setStatus(TrainingProgram.TrainingStatus.valueOf(request.getStatus()));
        }
        program.setMaxParticipants(request.getMaxParticipants());
        program.setPrerequisites(request.getPrerequisites());
        program.setLearningObjectives(request.getLearningObjectives());
        
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
            program.setInstructor(instructor);
        } else {
            program.setInstructor(null);
        }
        
        TrainingProgram updated = trainingProgramRepository.save(program);
        
        // Initialize only basic relationships needed for DTO (skip sessions/enrollments to avoid issues)
        initializeBasicRelationships(updated);
        
        return mapper.toDto(updated);
    }
    
    @Transactional
    public void deleteProgram(Long id) {
        TrainingProgram program = trainingProgramRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training program not found with id: " + id));

        // Manually remove child entities to avoid concurrent modification issues in Hibernate
        // Delete enrollments for this program
        enrollmentRepository.findByProgramId(id)
            .forEach(enrollmentRepository::delete);

        // Delete sessions for this program
        trainingSessionRepository.findByProgramId(id)
            .forEach(trainingSessionRepository::delete);

        // Now delete the program itself
        trainingProgramRepository.delete(program);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingProgramDto> getProgramsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return getAllPrograms();
        }
        try {
            TrainingProgram.TrainingStatus statusEnum = TrainingProgram.TrainingStatus.valueOf(status.trim().toUpperCase());
            List<TrainingProgram> programs = trainingProgramRepository.findByStatus(statusEnum);
            if (programs == null || programs.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            List<TrainingProgramDto> dtos = new java.util.ArrayList<>();
            for (TrainingProgram program : programs) {
                try {
                    initializeRelationships(program);
                    TrainingProgramDto dto = mapper.toDto(program);
                    if (dto != null) {
                        dtos.add(dto);
                    }
                } catch (Exception e) {
                    System.err.println("Error mapping program with ID " + program.getId() + ": " + e.getMessage());
                }
            }
            return dtos;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status + ". Valid values are: DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED, ARCHIVED");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new RuntimeException("Failed to retrieve programs by status: " + errorMsg, e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<TrainingProgramDto> getProgramsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllPrograms();
        }
        try {
            List<TrainingProgram> programs = trainingProgramRepository.findByCategory(category.trim());
            if (programs == null || programs.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            List<TrainingProgramDto> dtos = new java.util.ArrayList<>();
            for (TrainingProgram program : programs) {
                try {
                    initializeRelationships(program);
                    TrainingProgramDto dto = mapper.toDto(program);
                    if (dto != null) {
                        dtos.add(dto);
                    }
                } catch (Exception e) {
                    System.err.println("Error mapping program with ID " + program.getId() + ": " + e.getMessage());
                }
            }
            return dtos;
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + " occurred";
            }
            throw new RuntimeException("Failed to retrieve programs by category: " + errorMsg, e);
        }
    }
    
    @Transactional
    public TrainingProgramDto assignTeacher(AssignTeacherRequest request, String username) {
        // Verify current user exists (for audit/logging purposes)
        userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TrainingProgram program = trainingProgramRepository.findById(request.getProgramId())
            .orElseThrow(() -> new RuntimeException("Training program not found"));
        
        // Verify teacher exists and is a teacher
        StudentTeacher teacher = studentTeacherRepository.findByUserId(request.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (teacher.getType() != StudentTeacher.Type.TEACHER) {
            throw new RuntimeException("User is not a teacher");
        }
        
        if (!teacher.getIsActive()) {
            throw new RuntimeException("Teacher is not active");
        }
        
        // Assign teacher as instructor
        program.setInstructor(teacher.getUser());
        TrainingProgram updated = trainingProgramRepository.save(program);
        initializeRelationships(updated);
        return mapper.toDto(updated);
    }
    
    @Transactional
    public void assignStudents(AssignStudentRequest request, String username) {
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TrainingProgram program = trainingProgramRepository.findById(request.getProgramId())
            .orElseThrow(() -> new RuntimeException("Training program not found"));
        
        for (Long studentId : request.getStudentIds()) {
            // Verify student exists and is a student
            StudentTeacher student = studentTeacherRepository.findByUserId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
            
            if (student.getType() != StudentTeacher.Type.STUDENT) {
                throw new RuntimeException("User with ID " + studentId + " is not a student");
            }
            
            if (!student.getIsActive()) {
                throw new RuntimeException("Student with ID " + studentId + " is not active");
            }
            
            // Check if enrollment already exists
            enrollmentRepository.findByProgramIdAndParticipantId(program.getId(), studentId)
                .ifPresentOrElse(
                    enrollment -> {
                        // Enrollment exists, update if needed
                        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.CANCELLED || 
                            enrollment.getStatus() == Enrollment.EnrollmentStatus.WITHDRAWN) {
                            enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
                            enrollment.setEnrollmentDate(LocalDateTime.now());
                            enrollment.setEnrolledBy(currentUser);
                            enrollmentRepository.save(enrollment);
                        }
                    },
                    () -> {
                        // Create new enrollment
                        Enrollment enrollment = new Enrollment();
                        enrollment.setProgram(program);
                        enrollment.setParticipant(student.getUser());
                        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
                        enrollment.setEnrollmentDate(LocalDateTime.now());
                        enrollment.setEnrolledBy(currentUser);
                        enrollmentRepository.save(enrollment);
                    }
                );
        }
    }
    
    @Transactional
    public void removeStudentFromProgram(Long programId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findByProgramIdAndParticipantId(programId, studentId)
            .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.WITHDRAWN);
        enrollmentRepository.save(enrollment);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingProgramDto> getProgramsByInstructor(Long instructorId) {
        List<TrainingProgram> programs = trainingProgramRepository.findByInstructorId(instructorId);
        programs.forEach(this::initializeRelationships);
        return programs.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<com.example.CBS.Dashboard.dto.training.EnrollmentDto> getProgramEnrollments(Long programId) {
        List<Enrollment> enrollments = enrollmentRepository.findByProgramId(programId);
        enrollments.forEach(e -> {
            if (e.getSession() != null) {
                Hibernate.initialize(e.getSession());
            }
            if (e.getParticipant() != null) {
                Hibernate.initialize(e.getParticipant());
            }
            if (e.getEnrolledBy() != null) {
                Hibernate.initialize(e.getEnrolledBy());
            }
        });
        return enrollments.stream()
            .map(this::mapEnrollmentToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TrainingProgramDto> getProgramsByStudent(Long studentId) {
        List<TrainingProgram> programs = trainingProgramRepository.findByStudentId(studentId);
        programs.forEach(this::initializeRelationships);
        return programs.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    private com.example.CBS.Dashboard.dto.training.EnrollmentDto mapEnrollmentToDto(Enrollment enrollment) {
        com.example.CBS.Dashboard.dto.training.EnrollmentDto dto = new com.example.CBS.Dashboard.dto.training.EnrollmentDto();
        dto.setId(enrollment.getId());
        dto.setProgramId(enrollment.getProgram().getId());
        dto.setProgramTitle(enrollment.getProgram().getTitle());
        if (enrollment.getSession() != null) {
            dto.setSessionId(enrollment.getSession().getId());
            dto.setSessionStartDateTime(enrollment.getSession().getStartDateTime());
        }
        dto.setParticipantId(enrollment.getParticipant().getId());
        dto.setParticipantUsername(enrollment.getParticipant().getUsername());
        dto.setParticipantFullName(enrollment.getParticipant().getFullName());
        dto.setParticipantEmail(enrollment.getParticipant().getEmail());
        dto.setStatus(enrollment.getStatus().name());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setCompletionDate(enrollment.getCompletionDate());
        dto.setAttendancePercentage(enrollment.getAttendancePercentage());
        dto.setFinalScore(enrollment.getFinalScore());
        dto.setNotes(enrollment.getNotes());
        if (enrollment.getEnrolledBy() != null) {
            dto.setEnrolledById(enrollment.getEnrolledBy().getId());
            dto.setEnrolledByUsername(enrollment.getEnrolledBy().getUsername());
        }
        dto.setCreatedAt(enrollment.getCreatedAt());
        dto.setUpdatedAt(enrollment.getUpdatedAt());
        return dto;
    }
    
    /**
     * Initialize only basic relationships needed for DTO mapping.
     * This method skips sessions and enrollments to avoid lazy loading issues during create/update.
     */
    private void initializeBasicRelationships(TrainingProgram program) {
        if (program == null) {
            return;
        }
        try {
            if (program.getCreatedBy() != null) {
                Hibernate.initialize(program.getCreatedBy());
            }
            if (program.getInstructor() != null) {
                Hibernate.initialize(program.getInstructor());
            }
            if (program.getTrainingTopic() != null) {
                Hibernate.initialize(program.getTrainingTopic());
            }
            if (program.getTrainingName() != null) {
                Hibernate.initialize(program.getTrainingName());
            }
            if (program.getTrainingCategory() != null) {
                Hibernate.initialize(program.getTrainingCategory());
            }
            if (program.getDepartment() != null) {
                Hibernate.initialize(program.getDepartment());
            }
            if (program.getTrainingModule() != null) {
                Hibernate.initialize(program.getTrainingModule());
            }
            if (program.getCoordinator() != null) {
                Hibernate.initialize(program.getCoordinator());
                if (program.getCoordinator().getUser() != null) {
                    Hibernate.initialize(program.getCoordinator().getUser());
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Error initializing basic relationships for program " + (program != null ? program.getId() : "null") + ": " + e.getMessage());
            // Don't throw - continue processing
        }
    }
    
    private void initializeRelationships(TrainingProgram program) {
        if (program == null) {
            return;
        }
        try {
            // Initialize basic relationships first
            initializeBasicRelationships(program);
            
            // Then try to initialize collections (may fail, but that's ok)
            // Initialize sessions and enrollments for count calculation
            // Access the collections to trigger lazy loading
            try {
                if (program.getSessions() != null) {
                    Hibernate.initialize(program.getSessions());
                    // Trigger initialization by accessing size
                    program.getSessions().size();
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not initialize sessions for program " + program.getId() + ": " + e.getMessage());
            }
            try {
                if (program.getEnrollments() != null) {
                    Hibernate.initialize(program.getEnrollments());
                    // Trigger initialization by accessing size
                    program.getEnrollments().size();
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not initialize enrollments for program " + program.getId() + ": " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Warning: Error initializing relationships for program " + program.getId() + ": " + e.getMessage());
            // Don't throw - continue processing
        }
    }
}

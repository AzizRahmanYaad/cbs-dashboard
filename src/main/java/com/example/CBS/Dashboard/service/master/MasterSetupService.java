package com.example.CBS.Dashboard.service.master;

import com.example.CBS.Dashboard.dto.master.*;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterSetupService {
    
    private final TrainingTopicRepository trainingTopicRepository;
    private final TrainingNameRepository trainingNameRepository;
    private final TrainingModuleRepository trainingModuleRepository;
    private final TrainingCategoryMasterRepository trainingCategoryMasterRepository;
    private final DepartmentRepository departmentRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final StudentTeacherRepository studentTeacherRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    
    // Training Topic methods
    @Transactional
    public TrainingTopicDto createTrainingTopic(CreateTrainingTopicRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (trainingTopicRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training topic with this name already exists");
        }
        
        TrainingTopic topic = new TrainingTopic();
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        topic.setCreatedBy(user);
        
        TrainingTopic saved = trainingTopicRepository.save(topic);
        Hibernate.initialize(saved.getCreatedBy());
        
        return toTrainingTopicDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingTopicDto> getAllTrainingTopics(boolean activeOnly) {
        List<TrainingTopic> topics = activeOnly ? 
            trainingTopicRepository.findAllActive() : 
            trainingTopicRepository.findAll();
        topics.forEach(topic -> {
            if (topic.getCreatedBy() != null) {
                Hibernate.initialize(topic.getCreatedBy());
            }
        });
        return topics.stream().map(this::toTrainingTopicDto).collect(Collectors.toList());
    }
    
    @Transactional
    public TrainingTopicDto updateTrainingTopic(Long id, CreateTrainingTopicRequest request) {
        TrainingTopic topic = trainingTopicRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training topic not found"));
        
        if (!topic.getName().equals(request.getName()) && trainingTopicRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training topic with this name already exists");
        }
        
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            topic.setIsActive(request.getIsActive());
        }
        
        TrainingTopic updated = trainingTopicRepository.save(topic);
        return toTrainingTopicDto(updated);
    }
    
    @Transactional
    public void deleteTrainingTopic(Long id) {
        trainingTopicRepository.deleteById(id);
    }
    
    // Training Name methods
    @Transactional
    public TrainingNameDto createTrainingName(CreateTrainingNameRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (trainingNameRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training name with this name already exists");
        }
        
        TrainingName trainingName = new TrainingName();
        trainingName.setName(request.getName());
        trainingName.setDescription(request.getDescription());
        trainingName.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        trainingName.setCreatedBy(user);
        
        TrainingName saved = trainingNameRepository.save(trainingName);
        Hibernate.initialize(saved.getCreatedBy());
        
        return toTrainingNameDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingNameDto> getAllTrainingNames(boolean activeOnly) {
        List<TrainingName> names = activeOnly ? 
            trainingNameRepository.findAllActive() : 
            trainingNameRepository.findAll();
        names.forEach(name -> {
            if (name.getCreatedBy() != null) {
                Hibernate.initialize(name.getCreatedBy());
            }
        });
        return names.stream().map(this::toTrainingNameDto).collect(Collectors.toList());
    }
    
    @Transactional
    public TrainingNameDto updateTrainingName(Long id, CreateTrainingNameRequest request) {
        TrainingName trainingName = trainingNameRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training name not found"));
        
        if (!trainingName.getName().equals(request.getName()) && trainingNameRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training name with this name already exists");
        }
        
        trainingName.setName(request.getName());
        trainingName.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            trainingName.setIsActive(request.getIsActive());
        }
        
        TrainingName updated = trainingNameRepository.save(trainingName);
        return toTrainingNameDto(updated);
    }
    
    @Transactional
    public void deleteTrainingName(Long id) {
        trainingNameRepository.deleteById(id);
    }
    
    // Training Module methods
    @Transactional
    public TrainingModuleDto createTrainingModule(CreateTrainingModuleRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (trainingModuleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training module with this name already exists");
        }
        
        TrainingModule module = new TrainingModule();
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        module.setCreatedBy(user);
        
        TrainingModule saved = trainingModuleRepository.save(module);
        Hibernate.initialize(saved.getCreatedBy());
        
        return toTrainingModuleDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingModuleDto> getAllTrainingModules(boolean activeOnly) {
        List<TrainingModule> modules = activeOnly ? 
            trainingModuleRepository.findAllActive() : 
            trainingModuleRepository.findAll();
        modules.forEach(module -> {
            if (module.getCreatedBy() != null) {
                Hibernate.initialize(module.getCreatedBy());
            }
        });
        return modules.stream().map(this::toTrainingModuleDto).collect(Collectors.toList());
    }
    
    @Transactional
    public TrainingModuleDto updateTrainingModule(Long id, CreateTrainingModuleRequest request) {
        TrainingModule module = trainingModuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training module not found"));
        
        if (!module.getName().equals(request.getName()) && trainingModuleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training module with this name already exists");
        }
        
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            module.setIsActive(request.getIsActive());
        }
        
        TrainingModule updated = trainingModuleRepository.save(module);
        return toTrainingModuleDto(updated);
    }
    
    @Transactional
    public void deleteTrainingModule(Long id) {
        trainingModuleRepository.deleteById(id);
    }
    
    // Department methods
    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with this name already exists");
        }
        
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        department.setCreatedBy(user);
        
        Department saved = departmentRepository.save(department);
        Hibernate.initialize(saved.getCreatedBy());
        
        return toDepartmentDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments(boolean activeOnly) {
        List<Department> departments = activeOnly ? 
            departmentRepository.findAllActive() : 
            departmentRepository.findAll();
        departments.forEach(dept -> {
            if (dept.getCreatedBy() != null) {
                Hibernate.initialize(dept.getCreatedBy());
            }
        });
        return departments.stream().map(this::toDepartmentDto).collect(Collectors.toList());
    }
    
    @Transactional
    public DepartmentDto updateDepartment(Long id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found"));
        
        if (!department.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with this name already exists");
        }
        
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
        
        Department updated = departmentRepository.save(department);
        return toDepartmentDto(updated);
    }
    
    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
    
    // Training Category Master methods
    @Transactional
    public TrainingCategoryMasterDto createTrainingCategory(CreateTrainingCategoryMasterRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (trainingCategoryMasterRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training category with this name already exists");
        }
        
        TrainingCategoryMaster category = new TrainingCategoryMaster();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setCreatedBy(user);
        
        TrainingCategoryMaster saved = trainingCategoryMasterRepository.save(category);
        Hibernate.initialize(saved.getCreatedBy());
        
        return toTrainingCategoryDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingCategoryMasterDto> getAllTrainingCategories(boolean activeOnly) {
        List<TrainingCategoryMaster> categories = activeOnly ? 
            trainingCategoryMasterRepository.findAllActive() : 
            trainingCategoryMasterRepository.findAll();
        categories.forEach(cat -> {
            if (cat.getCreatedBy() != null) {
                Hibernate.initialize(cat.getCreatedBy());
            }
        });
        return categories.stream().map(this::toTrainingCategoryDto).collect(Collectors.toList());
    }
    
    @Transactional
    public TrainingCategoryMasterDto updateTrainingCategory(Long id, CreateTrainingCategoryMasterRequest request) {
        TrainingCategoryMaster category = trainingCategoryMasterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training category not found"));
        
        if (!category.getName().equals(request.getName()) && trainingCategoryMasterRepository.existsByName(request.getName())) {
            throw new RuntimeException("Training category with this name already exists");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        TrainingCategoryMaster updated = trainingCategoryMasterRepository.save(category);
        return toTrainingCategoryDto(updated);
    }
    
    @Transactional
    public void deleteTrainingCategory(Long id) {
        trainingCategoryMasterRepository.deleteById(id);
    }
    
    // Coordinator methods
    @Transactional
    public CoordinatorDto createCoordinator(CreateCoordinatorRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        User coordinatorUser = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (coordinatorRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Coordinator already exists for this user");
        }
        
        Coordinator coordinator = new Coordinator();
        coordinator.setUser(coordinatorUser);
        coordinator.setEmployeeId(request.getEmployeeId());
        coordinator.setDepartment(request.getDepartment());
        coordinator.setPhone(request.getPhone());
        coordinator.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        coordinator.setCreatedBy(user);
        
        Coordinator saved = coordinatorRepository.save(coordinator);
        Hibernate.initialize(saved.getUser());
        Hibernate.initialize(saved.getCreatedBy());
        
        return toCoordinatorDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<CoordinatorDto> getAllCoordinators(boolean activeOnly) {
        List<Coordinator> coordinators = activeOnly ? 
            coordinatorRepository.findAllActive() : 
            coordinatorRepository.findAll();
        coordinators.forEach(coord -> {
            if (coord.getUser() != null) {
                Hibernate.initialize(coord.getUser());
            }
            if (coord.getCreatedBy() != null) {
                Hibernate.initialize(coord.getCreatedBy());
            }
        });
        return coordinators.stream().map(this::toCoordinatorDto).collect(Collectors.toList());
    }
    
    @Transactional
    public CoordinatorDto updateCoordinator(Long id, CreateCoordinatorRequest request) {
        Coordinator coordinator = coordinatorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Coordinator not found"));
        
        coordinator.setEmployeeId(request.getEmployeeId());
        coordinator.setDepartment(request.getDepartment());
        coordinator.setPhone(request.getPhone());
        if (request.getIsActive() != null) {
            coordinator.setIsActive(request.getIsActive());
        }
        
        Coordinator updated = coordinatorRepository.save(coordinator);
        return toCoordinatorDto(updated);
    }
    
    @Transactional
    public void deleteCoordinator(Long id) {
        coordinatorRepository.deleteById(id);
    }
    
    // Student/Teacher methods
    @Transactional
    public StudentTeacherDto createStudentTeacher(CreateStudentTeacherRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        User studentTeacherUser = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (studentTeacherRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Student/Teacher already exists for this user");
        }
        
        StudentTeacher studentTeacher = new StudentTeacher();
        studentTeacher.setUser(studentTeacherUser);
        studentTeacher.setType(StudentTeacher.Type.valueOf(request.getType()));
        studentTeacher.setEmployeeId(request.getEmployeeId());
        studentTeacher.setStudentId(request.getStudentId());
        studentTeacher.setDepartment(request.getDepartment());
        studentTeacher.setPhone(request.getPhone());
        studentTeacher.setQualification(request.getQualification());
        studentTeacher.setSpecialization(request.getSpecialization());
        studentTeacher.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        studentTeacher.setCreatedBy(user);
        
        StudentTeacher saved = studentTeacherRepository.save(studentTeacher);
        Hibernate.initialize(saved.getUser());
        Hibernate.initialize(saved.getCreatedBy());
        
        return toStudentTeacherDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<StudentTeacherDto> getAllStudentTeachers(boolean activeOnly, String type) {
        List<StudentTeacher> studentTeachers;
        if (type != null && !type.isEmpty()) {
            StudentTeacher.Type enumType = StudentTeacher.Type.valueOf(type);
            studentTeachers = activeOnly ? 
                studentTeacherRepository.findAllActiveByType(enumType) : 
                studentTeacherRepository.findAll().stream()
                    .filter(st -> st.getType() == enumType)
                    .collect(Collectors.toList());
        } else {
            studentTeachers = activeOnly ? 
                studentTeacherRepository.findAllActive() : 
                studentTeacherRepository.findAll();
        }
        
        studentTeachers.forEach(st -> {
            if (st.getUser() != null) {
                Hibernate.initialize(st.getUser());
            }
            if (st.getCreatedBy() != null) {
                Hibernate.initialize(st.getCreatedBy());
            }
        });
        return studentTeachers.stream().map(this::toStudentTeacherDto).collect(Collectors.toList());
    }
    
    @Transactional
    public StudentTeacherDto updateStudentTeacher(Long id, CreateStudentTeacherRequest request) {
        StudentTeacher studentTeacher = studentTeacherRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student/Teacher not found"));
        
        studentTeacher.setType(StudentTeacher.Type.valueOf(request.getType()));
        studentTeacher.setEmployeeId(request.getEmployeeId());
        studentTeacher.setStudentId(request.getStudentId());
        studentTeacher.setDepartment(request.getDepartment());
        studentTeacher.setPhone(request.getPhone());
        studentTeacher.setQualification(request.getQualification());
        studentTeacher.setSpecialization(request.getSpecialization());
        if (request.getIsActive() != null) {
            studentTeacher.setIsActive(request.getIsActive());
        }
        
        StudentTeacher updated = studentTeacherRepository.save(studentTeacher);
        return toStudentTeacherDto(updated);
    }
    
    @Transactional
    public void deleteStudentTeacher(Long id) {
        StudentTeacher st = studentTeacherRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student/Teacher not found"));
        User user = st.getUser();
        if (user == null) {
            studentTeacherRepository.deleteById(id);
            return;
        }
        Long userId = user.getId();
        StudentTeacher.Type type = st.getType();

        // 1. Remove all enrollments (so they no longer appear in program/teacher student lists)
        enrollmentRepository.findByParticipantId(userId).forEach(enrollmentRepository::delete);

        if (type == StudentTeacher.Type.STUDENT) {
            // 2a. Remove attendance records for this student
            attendanceRepository.findByParticipantId(userId).forEach(attendanceRepository::delete);
            // 2b. Remove assessment results for this student
            assessmentResultRepository.findByParticipantId(userId).forEach(assessmentResultRepository::delete);
        } else {
            // 2. For teacher: clear instructor from programs and sessions so FK is not violated
            trainingProgramRepository.findByInstructorId(userId).forEach(program -> {
                program.setInstructor(null);
                trainingProgramRepository.save(program);
            });
            trainingSessionRepository.findByInstructorId(userId).forEach(session -> {
                session.setInstructor(null);
                trainingSessionRepository.save(session);
            });
        }

        // 3. Delete the student/teacher record
        studentTeacherRepository.delete(st);

        // 4. Remove the corresponding role so they cannot access student/teacher dashboard
        String roleToRemove = type == StudentTeacher.Type.STUDENT ? "ROLE_STUDENT" : "ROLE_TEACHER";
        user.getRoles().removeIf(r -> roleToRemove.equals(r.getName()));

        // 5. Disable the user so they cannot log in
        user.setEnabled(false);
        userRepository.save(user);
    }
    
    // DTO conversion methods
    private TrainingTopicDto toTrainingTopicDto(TrainingTopic topic) {
        TrainingTopicDto dto = new TrainingTopicDto();
        dto.setId(topic.getId());
        dto.setName(topic.getName());
        dto.setDescription(topic.getDescription());
        dto.setIsActive(topic.getIsActive());
        if (topic.getCreatedBy() != null) {
            dto.setCreatedById(topic.getCreatedBy().getId());
            dto.setCreatedByUsername(topic.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(topic.getCreatedAt());
        dto.setUpdatedAt(topic.getUpdatedAt());
        return dto;
    }
    
    private TrainingNameDto toTrainingNameDto(TrainingName name) {
        TrainingNameDto dto = new TrainingNameDto();
        dto.setId(name.getId());
        dto.setName(name.getName());
        dto.setDescription(name.getDescription());
        dto.setIsActive(name.getIsActive());
        if (name.getCreatedBy() != null) {
            dto.setCreatedById(name.getCreatedBy().getId());
            dto.setCreatedByUsername(name.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(name.getCreatedAt());
        dto.setUpdatedAt(name.getUpdatedAt());
        return dto;
    }
    
    private TrainingCategoryMasterDto toTrainingCategoryDto(TrainingCategoryMaster category) {
        TrainingCategoryMasterDto dto = new TrainingCategoryMasterDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        if (category.getCreatedBy() != null) {
            dto.setCreatedById(category.getCreatedBy().getId());
            dto.setCreatedByUsername(category.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
    
    private CoordinatorDto toCoordinatorDto(Coordinator coordinator) {
        CoordinatorDto dto = new CoordinatorDto();
        dto.setId(coordinator.getId());
        if (coordinator.getUser() != null) {
            dto.setUserId(coordinator.getUser().getId());
            dto.setUsername(coordinator.getUser().getUsername());
            dto.setFullName(coordinator.getUser().getFullName());
            dto.setEmail(coordinator.getUser().getEmail());
        }
        dto.setEmployeeId(coordinator.getEmployeeId());
        dto.setDepartment(coordinator.getDepartment());
        dto.setPhone(coordinator.getPhone());
        dto.setIsActive(coordinator.getIsActive());
        if (coordinator.getCreatedBy() != null) {
            dto.setCreatedById(coordinator.getCreatedBy().getId());
            dto.setCreatedByUsername(coordinator.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(coordinator.getCreatedAt());
        dto.setUpdatedAt(coordinator.getUpdatedAt());
        return dto;
    }
    
    private StudentTeacherDto toStudentTeacherDto(StudentTeacher studentTeacher) {
        StudentTeacherDto dto = new StudentTeacherDto();
        dto.setId(studentTeacher.getId());
        if (studentTeacher.getUser() != null) {
            dto.setUserId(studentTeacher.getUser().getId());
            dto.setUsername(studentTeacher.getUser().getUsername());
            dto.setFullName(studentTeacher.getUser().getFullName());
            dto.setEmail(studentTeacher.getUser().getEmail());
        }
        dto.setType(studentTeacher.getType().name());
        dto.setEmployeeId(studentTeacher.getEmployeeId());
        dto.setStudentId(studentTeacher.getStudentId());
        dto.setDepartment(studentTeacher.getDepartment());
        dto.setPhone(studentTeacher.getPhone());
        dto.setQualification(studentTeacher.getQualification());
        dto.setSpecialization(studentTeacher.getSpecialization());
        dto.setIsActive(studentTeacher.getIsActive());
        if (studentTeacher.getCreatedBy() != null) {
            dto.setCreatedById(studentTeacher.getCreatedBy().getId());
            dto.setCreatedByUsername(studentTeacher.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(studentTeacher.getCreatedAt());
        dto.setUpdatedAt(studentTeacher.getUpdatedAt());
        return dto;
    }
    
    private DepartmentDto toDepartmentDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setIsActive(department.getIsActive());
        if (department.getCreatedBy() != null) {
            dto.setCreatedById(department.getCreatedBy().getId());
            dto.setCreatedByUsername(department.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(department.getCreatedAt());
        dto.setUpdatedAt(department.getUpdatedAt());
        return dto;
    }
    
    private TrainingModuleDto toTrainingModuleDto(TrainingModule module) {
        TrainingModuleDto dto = new TrainingModuleDto();
        dto.setId(module.getId());
        dto.setName(module.getName());
        dto.setDescription(module.getDescription());
        dto.setIsActive(module.getIsActive());
        if (module.getCreatedBy() != null) {
            dto.setCreatedById(module.getCreatedBy().getId());
            dto.setCreatedByUsername(module.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        return dto;
    }
}

package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.CreateTrainingSessionRequest;
import com.example.CBS.Dashboard.dto.training.TrainingSessionDto;
import com.example.CBS.Dashboard.entity.TrainingProgram;
import com.example.CBS.Dashboard.entity.TrainingSession;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.TrainingProgramRepository;
import com.example.CBS.Dashboard.repository.TrainingSessionRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingSessionService {
    
    private final TrainingSessionRepository sessionRepository;
    private final TrainingProgramRepository programRepository;
    private final UserRepository userRepository;
    private final TrainingAccessService trainingAccessService;
    
    @Transactional
    public TrainingSessionDto createSession(CreateTrainingSessionRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TrainingProgram program = programRepository.findById(request.getProgramId())
            .orElseThrow(() -> new RuntimeException("Training program not found"));
        
        TrainingSession session = new TrainingSession();
        session.setProgram(program);
        session.setStartDateTime(request.getStartDateTime());
        session.setEndDateTime(request.getEndDateTime());
        session.setLocation(request.getLocation());
        session.setSessionType(request.getSessionType());
        session.setStatus(request.getStatus() != null ? 
            TrainingSession.SessionStatus.valueOf(request.getStatus()) : 
            TrainingSession.SessionStatus.SCHEDULED);
        session.setMaxCapacity(request.getMaxCapacity());
        session.setNotes(request.getNotes());
        session.setTopic(request.getTopic());
        session.setCreatedBy(user);
        
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
            session.setInstructor(instructor);
        } else {
            // Default to program instructor if available
            session.setInstructor(program.getInstructor());
        }
        
        TrainingSession saved = sessionRepository.save(session);
        return mapToDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingSessionDto> getAllSessions() {
        List<TrainingSessionDto> sessions = sessionRepository.findAll().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        // Auto-generate sequence order in descending order
        assignSequenceOrder(sessions);
        return sessions;
    }
    
    @Transactional(readOnly = true)
    public List<TrainingSessionDto> getSessionsByProgram(Long programId) {
        List<TrainingSessionDto> sessions = sessionRepository.findByProgramId(programId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        // Auto-generate sequence order in descending order
        assignSequenceOrder(sessions);
        return sessions;
    }
    
    private void assignSequenceOrder(List<TrainingSessionDto> sessions) {
        int sequence = sessions.size();
        for (TrainingSessionDto session : sessions) {
            session.setSequenceOrder(sequence--);
        }
    }
    
    @Transactional(readOnly = true)
    public TrainingSessionDto getSessionById(Long id, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        TrainingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training session not found"));
        // Admins and training admins can see any session.
        if (!hasAnyRole(user, "ROLE_ADMIN", "ROLE_TRAINING_ADMIN")) {
            if (session.getProgram() == null || session.getProgram().getId() == null) {
                throw new RuntimeException("Training session is not linked to a training program");
            }
            trainingAccessService.assertCanAccessProgramContent(username, session.getProgram().getId());
        }
        return mapToDto(session);
    }

    /**
     * Returns sessions visible to the given user.
     *
     * - Admin / training admin:
     *     - with programId: all sessions for that program
     *     - without programId: all sessions
     * - Other users (teachers/students):
     *     - with programId: only if they are instructor or enrolled in the program
     *     - without programId: sessions for programs where they are instructor or enrolled
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionDto> getSessionsForUser(String username, Long programId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        boolean isAdminLike = hasAnyRole(user, "ROLE_ADMIN", "ROLE_TRAINING_ADMIN");

        if (programId != null) {
            if (!isAdminLike) {
                trainingAccessService.assertCanAccessProgramContent(username, programId);
            }
            List<TrainingSessionDto> sessions = sessionRepository.findByProgramId(programId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
            assignSequenceOrder(sessions);
            return sessions;
        }

        // No program filter:
        if (isAdminLike) {
            // Admin-level users can see everything.
            return getAllSessions();
        }

        // Teachers/students: only sessions for programs where they are instructor or enrolled.
        Set<Long> accessibleProgramIds = new HashSet<>();

        // As instructor
        programRepository.findByInstructorId(user.getId())
            .forEach(p -> accessibleProgramIds.add(p.getId()));

        // As enrolled student (active enrollments only are already enforced in repository)
        programRepository.findByStudentId(user.getId())
            .forEach(p -> accessibleProgramIds.add(p.getId()));

        if (accessibleProgramIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<TrainingSessionDto> result = new ArrayList<>();
        for (Long pid : accessibleProgramIds) {
            List<TrainingSessionDto> programSessions = sessionRepository.findByProgramId(pid).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
            result.addAll(programSessions);
        }

        assignSequenceOrder(result);
        return result;
    }
    
    @Transactional
    public TrainingSessionDto updateSession(Long id, CreateTrainingSessionRequest request) {
        TrainingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training session not found"));
        
        session.setStartDateTime(request.getStartDateTime());
        session.setEndDateTime(request.getEndDateTime());
        session.setLocation(request.getLocation());
        session.setSessionType(request.getSessionType());
        if (request.getStatus() != null) {
            session.setStatus(TrainingSession.SessionStatus.valueOf(request.getStatus()));
        }
        session.setMaxCapacity(request.getMaxCapacity());
        session.setNotes(request.getNotes());
        session.setTopic(request.getTopic());
        
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
            session.setInstructor(instructor);
        }
        
        TrainingSession saved = sessionRepository.save(session);
        return mapToDto(saved);
    }

    private boolean hasAnyRole(User user, String... roleNames) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        return user.getRoles().stream()
            .map(role -> role.getName() != null ? role.getName().trim() : "")
            .anyMatch(userRole ->
                java.util.Arrays.stream(roleNames)
                    .anyMatch(required -> required.equalsIgnoreCase(userRole)));
    }
    
    @Transactional
    public void deleteSession(Long id) {
        TrainingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training session not found"));
        sessionRepository.delete(session);
    }
    
    private TrainingSessionDto mapToDto(TrainingSession session) {
        TrainingSessionDto dto = new TrainingSessionDto();
        dto.setId(session.getId());
        dto.setProgramId(session.getProgram().getId());
        dto.setProgramTitle(session.getProgram().getTitle());
        dto.setTopicName(session.getTopic());
        dto.setStartDateTime(session.getStartDateTime());
        dto.setEndDateTime(session.getEndDateTime());
        dto.setLocation(session.getLocation());
        dto.setSessionType(session.getSessionType());
        dto.setStatus(session.getStatus().name());
        dto.setMaxCapacity(session.getMaxCapacity());
        dto.setNotes(session.getNotes());
        if (session.getInstructor() != null) {
            dto.setInstructorId(session.getInstructor().getId());
            dto.setInstructorUsername(session.getInstructor().getUsername());
            dto.setInstructorFullName(session.getInstructor().getFullName());
        }
        dto.setCreatedById(session.getCreatedBy().getId());
        dto.setCreatedByUsername(session.getCreatedBy().getUsername());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        dto.setEnrollmentsCount(session.getEnrollments() != null ? session.getEnrollments().size() : 0);
        dto.setSequenceOrder(session.getSequenceOrder());
        return dto;
    }
}

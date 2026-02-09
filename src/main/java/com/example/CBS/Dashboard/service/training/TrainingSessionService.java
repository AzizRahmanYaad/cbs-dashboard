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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingSessionService {
    
    private final TrainingSessionRepository sessionRepository;
    private final TrainingProgramRepository programRepository;
    private final UserRepository userRepository;
    
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
    public TrainingSessionDto getSessionById(Long id) {
        TrainingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training session not found"));
        return mapToDto(session);
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
        
        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
            session.setInstructor(instructor);
        }
        
        TrainingSession saved = sessionRepository.save(session);
        return mapToDto(saved);
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

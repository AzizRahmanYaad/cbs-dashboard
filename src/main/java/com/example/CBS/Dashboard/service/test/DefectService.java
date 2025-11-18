package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.CreateDefectRequest;
import com.example.CBS.Dashboard.dto.test.DefectDto;
import com.example.CBS.Dashboard.dto.test.UpdateDefectRequest;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.mapper.DefectMapper;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefectService {
    
    private final DefectRepository defectRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final UserRepository userRepository;
    private final DefectMapper mapper;
    private final AuditLogService auditLogService;
    
    @Transactional
    public DefectDto createDefect(CreateDefectRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        Defect defect = new Defect();
        defect.setTitle(request.getTitle());
        defect.setDescription(request.getDescription());
        defect.setSeverity(request.getSeverity());
        defect.setStatus(Defect.DefectStatus.NEW);
        defect.setReportedBy(user);
        defect.setAttachments(request.getAttachments());
        
        if (request.getTestCaseId() != null) {
            TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + request.getTestCaseId()));
            defect.setTestCase(testCase);
        }
        
        if (request.getTestExecutionId() != null) {
            TestExecution execution = testExecutionRepository.findById(request.getTestExecutionId())
                .orElseThrow(() -> new RuntimeException("Test execution not found with id: " + request.getTestExecutionId()));
            defect.setTestExecution(execution);
        }
        
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getAssignedToId()));
            defect.setAssignedTo(assignedTo);
        }
        
        Defect saved = defectRepository.save(defect);
        auditLogService.logAction("DEFECT", saved.getId(), "CREATE", user, null, 
            "Created defect: " + saved.getTitle());
        
        return mapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<DefectDto> getAllDefects() {
        return defectRepository.findAll().stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public DefectDto getDefectById(Long id) {
        Defect defect = defectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found with id: " + id));
        return mapper.toDto(defect);
    }
    
    @Transactional(readOnly = true)
    public List<DefectDto> getDefectsByFilters(Defect.DefectStatus status, 
                                               Defect.DefectSeverity severity, Long assignedToId) {
        return defectRepository.findByFilters(status, severity, assignedToId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public DefectDto updateDefect(Long id, UpdateDefectRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        Defect defect = defectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found with id: " + id));
        
        String oldValue = defect.toString();
        
        if (request.getTitle() != null) defect.setTitle(request.getTitle());
        if (request.getDescription() != null) defect.setDescription(request.getDescription());
        if (request.getSeverity() != null) defect.setSeverity(request.getSeverity());
        if (request.getStatus() != null) defect.setStatus(request.getStatus());
        
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getAssignedToId()));
            defect.setAssignedTo(assignedTo);
        }
        
        Defect updated = defectRepository.save(defect);
        auditLogService.logAction("DEFECT", updated.getId(), "UPDATE", user, oldValue, 
            "Updated defect: " + updated.getTitle());
        
        return mapper.toDto(updated);
    }
    
    @Transactional
    public void deleteDefect(Long id, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        Defect defect = defectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found with id: " + id));
        
        auditLogService.logAction("DEFECT", id, "DELETE", user, defect.toString(), 
            "Deleted defect: " + defect.getTitle());
        
        defectRepository.deleteById(id);
    }
}


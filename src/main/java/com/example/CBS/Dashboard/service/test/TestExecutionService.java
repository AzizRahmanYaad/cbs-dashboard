package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.CreateTestExecutionRequest;
import com.example.CBS.Dashboard.dto.test.TestExecutionDto;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.mapper.TestExecutionMapper;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestExecutionService {
    
    private final TestExecutionRepository testExecutionRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final TestExecutionMapper mapper;
    private final AuditLogService auditLogService;
    
    @Transactional
    public TestExecutionDto createExecution(CreateTestExecutionRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
            .orElseThrow(() -> new RuntimeException("Test case not found with id: " + request.getTestCaseId()));
        
        TestExecution execution = new TestExecution();
        execution.setTestCase(testCase);
        execution.setExecutedBy(user);
        execution.setStatus(request.getStatus());
        execution.setComments(request.getComments());
        execution.setAttachments(request.getAttachments());
        
        TestExecution saved = testExecutionRepository.save(execution);
        auditLogService.logAction("TEST_EXECUTION", saved.getId(), "EXECUTE", user, null, 
            "Executed test case: " + testCase.getTitle() + " with status: " + request.getStatus());
        
        return mapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TestExecutionDto> getAllExecutions() {
        return testExecutionRepository.findAll().stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TestExecutionDto getExecutionById(Long id) {
        TestExecution execution = testExecutionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test execution not found with id: " + id));
        return mapper.toDto(execution);
    }
    
    @Transactional(readOnly = true)
    public List<TestExecutionDto> getExecutionsByTestCaseId(Long testCaseId) {
        return testExecutionRepository.findByTestCaseId(testCaseId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TestExecutionDto> getExecutionsByUserId(Long userId) {
        return testExecutionRepository.findByExecutedById(userId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TestExecutionDto> getExecutionsByStatus(TestExecution.ExecutionStatus status) {
        return testExecutionRepository.findByStatus(status).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
}


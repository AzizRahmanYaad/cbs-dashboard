package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.CreateTestCaseRequest;
import com.example.CBS.Dashboard.dto.test.TestCaseDto;
import com.example.CBS.Dashboard.dto.test.UpdateTestCaseRequest;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.mapper.TestCaseMapper;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestCaseService {
    
    private final TestCaseRepository testCaseRepository;
    private final TestModuleRepository testModuleRepository;
    private final UserRepository userRepository;
    private final TestCaseMapper mapper;
    private final AuditLogService auditLogService;
    
    @Transactional
    public TestCaseDto createTestCase(CreateTestCaseRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TestCase testCase = new TestCase();
        testCase.setTitle(request.getTitle());
        testCase.setPreconditions(request.getPreconditions());
        testCase.setSteps(request.getSteps());
        testCase.setExpectedResult(request.getExpectedResult());
        testCase.setPriority(request.getPriority());
        testCase.setStatus(TestCase.TestCaseStatus.DRAFT);
        testCase.setCreatedBy(user);
        
        if (request.getModuleId() != null) {
            TestModule module = testModuleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + request.getModuleId()));
            testCase.setModule(module);
        }
        
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getAssignedToId()));
            testCase.setAssignedTo(assignedTo);
        }
        
        TestCase saved = testCaseRepository.save(testCase);
        auditLogService.logAction("TEST_CASE", saved.getId(), "CREATE", user, null, 
            "Created test case: " + saved.getTitle());
        
        return mapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TestCaseDto> getAllTestCases() {
        return testCaseRepository.findAll().stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TestCaseDto getTestCaseById(Long id) {
        TestCase testCase = testCaseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));
        return mapper.toDto(testCase);
    }
    
    @Transactional(readOnly = true)
    public List<TestCaseDto> getTestCasesByFilters(Long moduleId, TestCase.TestCaseStatus status, 
                                                   Long assignedToId, TestCase.Priority priority) {
        return testCaseRepository.findByFilters(moduleId, status, assignedToId, priority).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TestCaseDto> searchTestCases(String searchTerm) {
        return testCaseRepository.searchByTitleOrPreconditions(searchTerm).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public TestCaseDto updateTestCase(Long id, UpdateTestCaseRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TestCase testCase = testCaseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));
        
        String oldValue = testCase.toString();
        
        if (request.getTitle() != null) testCase.setTitle(request.getTitle());
        if (request.getPreconditions() != null) testCase.setPreconditions(request.getPreconditions());
        if (request.getSteps() != null) testCase.setSteps(request.getSteps());
        if (request.getExpectedResult() != null) testCase.setExpectedResult(request.getExpectedResult());
        if (request.getPriority() != null) testCase.setPriority(request.getPriority());
        if (request.getStatus() != null) testCase.setStatus(request.getStatus());
        
        if (request.getModuleId() != null) {
            TestModule module = testModuleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + request.getModuleId()));
            testCase.setModule(module);
        }
        
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getAssignedToId()));
            testCase.setAssignedTo(assignedTo);
        }
        
        TestCase updated = testCaseRepository.save(testCase);
        auditLogService.logAction("TEST_CASE", updated.getId(), "UPDATE", user, oldValue, 
            "Updated test case: " + updated.getTitle());
        
        return mapper.toDto(updated);
    }
    
    @Transactional
    public void deleteTestCase(Long id, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TestCase testCase = testCaseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test case not found with id: " + id));
        
        auditLogService.logAction("TEST_CASE", id, "DELETE", user, testCase.toString(), 
            "Deleted test case: " + testCase.getTitle());
        
        testCaseRepository.deleteById(id);
    }
}


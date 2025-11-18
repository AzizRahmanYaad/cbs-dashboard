package com.example.CBS.Dashboard.controller.test;

import com.example.CBS.Dashboard.dto.test.CreateTestCaseRequest;
import com.example.CBS.Dashboard.dto.test.TestCaseDto;
import com.example.CBS.Dashboard.dto.test.UpdateTestCaseRequest;
import com.example.CBS.Dashboard.entity.TestCase;
import com.example.CBS.Dashboard.service.test.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/test-cases")
@RequiredArgsConstructor
public class TestCaseController {
    
    private final TestCaseService testCaseService;
    
    @PostMapping
    public ResponseEntity<TestCaseDto> createTestCase(
            @Valid @RequestBody CreateTestCaseRequest request,
            Authentication authentication) {
        TestCaseDto testCase = testCaseService.createTestCase(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(testCase);
    }
    
    @GetMapping
    public ResponseEntity<List<TestCaseDto>> getAllTestCases(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) TestCase.TestCaseStatus status,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) TestCase.Priority priority) {
        List<TestCaseDto> testCases;
        if (moduleId != null || status != null || assignedToId != null || priority != null) {
            testCases = testCaseService.getTestCasesByFilters(moduleId, status, assignedToId, priority);
        } else {
            testCases = testCaseService.getAllTestCases();
        }
        return ResponseEntity.ok(testCases);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<TestCaseDto>> searchTestCases(@RequestParam String searchTerm) {
        List<TestCaseDto> testCases = testCaseService.searchTestCases(searchTerm);
        return ResponseEntity.ok(testCases);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestCaseDto> getTestCaseById(@PathVariable Long id) {
        TestCaseDto testCase = testCaseService.getTestCaseById(id);
        return ResponseEntity.ok(testCase);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestCaseDto> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTestCaseRequest request,
            Authentication authentication) {
        TestCaseDto testCase = testCaseService.updateTestCase(id, request, authentication.getName());
        return ResponseEntity.ok(testCase);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable Long id,
            Authentication authentication) {
        testCaseService.deleteTestCase(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}


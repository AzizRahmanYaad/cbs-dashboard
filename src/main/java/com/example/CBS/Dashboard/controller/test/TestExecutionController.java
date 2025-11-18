package com.example.CBS.Dashboard.controller.test;

import com.example.CBS.Dashboard.dto.test.CreateTestExecutionRequest;
import com.example.CBS.Dashboard.dto.test.TestExecutionDto;
import com.example.CBS.Dashboard.entity.TestExecution;
import com.example.CBS.Dashboard.service.test.TestExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/executions")
@RequiredArgsConstructor
public class TestExecutionController {
    
    private final TestExecutionService testExecutionService;
    
    @PostMapping
    public ResponseEntity<TestExecutionDto> createExecution(
            @Valid @RequestBody CreateTestExecutionRequest request,
            Authentication authentication) {
        TestExecutionDto execution = testExecutionService.createExecution(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(execution);
    }
    
    @GetMapping
    public ResponseEntity<List<TestExecutionDto>> getAllExecutions(
            @RequestParam(required = false) Long testCaseId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TestExecution.ExecutionStatus status) {
        List<TestExecutionDto> executions;
        if (testCaseId != null) {
            executions = testExecutionService.getExecutionsByTestCaseId(testCaseId);
        } else if (userId != null) {
            executions = testExecutionService.getExecutionsByUserId(userId);
        } else if (status != null) {
            executions = testExecutionService.getExecutionsByStatus(status);
        } else {
            executions = testExecutionService.getAllExecutions();
        }
        return ResponseEntity.ok(executions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestExecutionDto> getExecutionById(@PathVariable Long id) {
        TestExecutionDto execution = testExecutionService.getExecutionById(id);
        return ResponseEntity.ok(execution);
    }
}


package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.TestReportDto;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestReportService {
    
    private final TestCaseRepository testCaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final DefectRepository defectRepository;
    
    @Transactional(readOnly = true)
    public TestReportDto generateReport(Long moduleId) {
        TestReportDto report = new TestReportDto();
        
        List<TestCase> testCases;
        if (moduleId != null) {
            testCases = testCaseRepository.findByModuleId(moduleId);
        } else {
            testCases = testCaseRepository.findAll();
        }
        
        report.setTotalTestCases((long) testCases.size());
        
        List<TestExecution> executions;
        if (moduleId != null) {
            executions = testExecutionRepository.findByModuleId(moduleId);
        } else {
            executions = testExecutionRepository.findAll();
        }
        
        report.setTotalExecutions((long) executions.size());
        
        report.setPassedCount(executions.stream()
            .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.PASSED)
            .count());
        
        report.setFailedCount(executions.stream()
            .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.FAILED)
            .count());
        
        report.setBlockedCount(executions.stream()
            .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.BLOCKED)
            .count());
        
        report.setRetestCount(executions.stream()
            .filter(e -> e.getStatus() == TestExecution.ExecutionStatus.RETEST)
            .count());
        
        List<Defect> defects = defectRepository.findAll();
        report.setTotalDefects((long) defects.size());
        
        // Status distribution
        Map<String, Long> statusDist = new HashMap<>();
        testCases.stream()
            .collect(Collectors.groupingBy(tc -> tc.getStatus().name(), Collectors.counting()))
            .forEach(statusDist::put);
        report.setStatusDistribution(statusDist);
        
        // Priority distribution
        Map<String, Long> priorityDist = new HashMap<>();
        testCases.stream()
            .collect(Collectors.groupingBy(tc -> tc.getPriority().name(), Collectors.counting()))
            .forEach(priorityDist::put);
        report.setPriorityDistribution(priorityDist);
        
        // Module distribution
        Map<String, Long> moduleDist = new HashMap<>();
        testCases.stream()
            .filter(tc -> tc.getModule() != null)
            .collect(Collectors.groupingBy(tc -> tc.getModule().getName(), Collectors.counting()))
            .forEach(moduleDist::put);
        report.setModuleDistribution(moduleDist);
        
        // Defect status distribution
        Map<String, Long> defectStatusDist = new HashMap<>();
        defects.stream()
            .collect(Collectors.groupingBy(d -> d.getStatus().name(), Collectors.counting()))
            .forEach(defectStatusDist::put);
        report.setDefectStatusDistribution(defectStatusDist);
        
        // Defect severity distribution
        Map<String, Long> defectSeverityDist = new HashMap<>();
        defects.stream()
            .collect(Collectors.groupingBy(d -> d.getSeverity().name(), Collectors.counting()))
            .forEach(defectSeverityDist::put);
        report.setDefectSeverityDistribution(defectSeverityDist);
        
        return report;
    }
}


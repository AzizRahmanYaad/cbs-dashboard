package com.example.CBS.Dashboard.controller.test;

import com.example.CBS.Dashboard.dto.test.TestReportDto;
import com.example.CBS.Dashboard.service.test.TestReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/reports")
@RequiredArgsConstructor
public class TestReportController {
    
    private final TestReportService testReportService;
    
    @GetMapping
    public ResponseEntity<TestReportDto> generateReport(@RequestParam(required = false) Long moduleId) {
        TestReportDto report = testReportService.generateReport(moduleId);
        return ResponseEntity.ok(report);
    }
}


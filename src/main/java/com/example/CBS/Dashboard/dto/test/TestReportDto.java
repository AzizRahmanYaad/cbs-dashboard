package com.example.CBS.Dashboard.dto.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestReportDto {
    private Long totalTestCases;
    private Long totalExecutions;
    private Long passedCount;
    private Long failedCount;
    private Long blockedCount;
    private Long retestCount;
    private Long totalDefects;
    private Map<String, Long> statusDistribution = new HashMap<>();
    private Map<String, Long> priorityDistribution = new HashMap<>();
    private Map<String, Long> moduleDistribution = new HashMap<>();
    private Map<String, Long> defectStatusDistribution = new HashMap<>();
    private Map<String, Long> defectSeverityDistribution = new HashMap<>();
}


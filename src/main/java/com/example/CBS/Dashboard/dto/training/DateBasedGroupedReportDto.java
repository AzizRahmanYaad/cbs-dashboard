package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateBasedGroupedReportDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<SessionAttendanceReportDto> sessionsByDate;   // Sessions in range
    private List<StudentParticipationDto> byStudent;           // Grouped by student
    private int totalSessions;
    private int totalStudents;
    private double overallParticipationRate;
}

package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentParticipationDto {
    private Long participantId;
    private String fullName;
    private String email;
    private int sessionsAttended;
    private int totalSessions;
    private double attendancePercent;
    private List<String> attendedSessionTopics;  // Topics of sessions they attended
    private String participationTrend;           // "Good", "Moderate", "Needs Attention"
}

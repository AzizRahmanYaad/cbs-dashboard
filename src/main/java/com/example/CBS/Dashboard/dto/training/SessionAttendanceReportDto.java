package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionAttendanceReportDto {
    private Long sessionId;
    private Long programId;
    private String programTitle;
    private String sessionTopic;
    private LocalDateTime startDateTime;
    private List<String> attendedStudentNames;
    /** Signatures of attended students for report authentication. */
    private List<AttendeeSignatureDto> attendedStudentSignatures;
    private String instructorName;
    /** Instructor's e-signature (base64) for this session. */
    private String instructorSignatureData;
    private String sessionType;   // e.g. In-Person, Virtual, Hybrid
    private String notes;
}

package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.DateBasedGroupedReportDto;
import com.example.CBS.Dashboard.dto.training.SessionAttendanceReportDto;
import com.example.CBS.Dashboard.dto.training.SingleSessionReportDto;
import com.example.CBS.Dashboard.service.training.TeacherReportService;
import com.example.CBS.Dashboard.service.training.TrainingReportPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/training/reports")
@RequiredArgsConstructor
public class TrainingReportController {

    private final TeacherReportService teacherReportService;
    private final TrainingReportPdfService trainingReportPdfService;

    @GetMapping("/teacher")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO')")
    public ResponseEntity<List<SessionAttendanceReportDto>> getTeacherReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        boolean isCfo = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CFO".equals(a.getAuthority()));
        List<SessionAttendanceReportDto> report = isCfo
                ? teacherReportService.getCfoAttendanceReport(from, to)
                : teacherReportService.getTeacherAttendanceReport(authentication.getName(), from, to);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/teacher/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO')")
    public ResponseEntity<Resource> downloadTeacherReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) throws IOException {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        boolean isCfo = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CFO".equals(a.getAuthority()));
        List<SessionAttendanceReportDto> reportRows = isCfo
                ? teacherReportService.getCfoAttendanceReport(from, to)
                : teacherReportService.getTeacherAttendanceReport(authentication.getName(), from, to);
        byte[] pdfBytes = trainingReportPdfService.generateAttendanceReportPdf(reportRows, from, to);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "attendance_report_" + from.format(DateTimeFormatter.ISO_LOCAL_DATE)
                + "_to_" + to.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO')")
    public ResponseEntity<SingleSessionReportDto> getSingleSessionReport(
            @PathVariable Long sessionId,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        boolean isCfo = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CFO".equals(a.getAuthority()));
        SingleSessionReportDto report = isCfo
                ? teacherReportService.getSingleSessionReportForCfo(sessionId)
                : teacherReportService.getSingleSessionReport(authentication.getName(), sessionId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/session/{sessionId}/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO')")
    public ResponseEntity<Resource> downloadSingleSessionReportPdf(
            @PathVariable Long sessionId,
            Authentication authentication) throws IOException {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        boolean isCfo = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CFO".equals(a.getAuthority()));
        SingleSessionReportDto report = isCfo
                ? teacherReportService.getSingleSessionReportForCfo(sessionId)
                : teacherReportService.getSingleSessionReport(authentication.getName(), sessionId);
        byte[] pdfBytes = trainingReportPdfService.generateSingleSessionReportPdf(report);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "session_report_" + sessionId + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/teacher/grouped")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO')")
    public ResponseEntity<DateBasedGroupedReportDto> getDateBasedGroupedReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        boolean isCfo = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CFO".equals(a.getAuthority()));
        DateBasedGroupedReportDto report = isCfo
                ? teacherReportService.getDateBasedGroupedReportForCfo(from, to)
                : teacherReportService.getDateBasedGroupedReport(authentication.getName(), from, to);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/teacher/grouped/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN', 'ROLE_CFO')")
    public ResponseEntity<Resource> downloadDateBasedGroupedReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) throws IOException {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        boolean isCfo = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CFO".equals(a.getAuthority()));
        DateBasedGroupedReportDto report = isCfo
                ? teacherReportService.getDateBasedGroupedReportForCfo(from, to)
                : teacherReportService.getDateBasedGroupedReport(authentication.getName(), from, to);
        byte[] pdfBytes = trainingReportPdfService.generateDateBasedGroupedReportPdf(report);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "grouped_report_" + from.format(DateTimeFormatter.ISO_LOCAL_DATE)
                + "_to_" + to.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}

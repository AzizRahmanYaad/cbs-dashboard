package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.AttendeeSignatureDto;
import com.example.CBS.Dashboard.dto.training.DateBasedGroupedReportDto;
import com.example.CBS.Dashboard.dto.training.SessionAttendanceReportDto;
import com.example.CBS.Dashboard.dto.training.SingleSessionReportDto;
import com.example.CBS.Dashboard.dto.training.StudentEngagementDto;
import com.example.CBS.Dashboard.dto.training.StudentParticipationDto;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingReportPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public byte[] generateAttendanceReportPdf(List<SessionAttendanceReportDto> reportRows,
                                              java.time.LocalDate from,
                                              java.time.LocalDate to) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(40, 40, 40, 40);

        if (reportRows == null || reportRows.isEmpty()) {
            addEmptyReport(document, from, to);
        } else {
            for (int i = 0; i < reportRows.size(); i++) {
                addTrainingSessionForm(document, reportRows.get(i), i + 1);
                if (i < reportRows.size() - 1) {
                    document.add(new Paragraph("\n"));
                    document.add(new com.itextpdf.layout.element.AreaBreak(com.itextpdf.layout.properties.AreaBreakType.NEXT_PAGE));
                }
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addEmptyReport(Document document, java.time.LocalDate from, java.time.LocalDate to) throws IOException {
        addHeader(document, null, "N/A", from);
        document.add(new Paragraph("No sessions found for the selected date range (" + from.format(DATE_FORMAT) + " — " + to.format(DATE_FORMAT) + ").")
                .setFontSize(11)
                .setMarginTop(20));
        addGuidelinesAndSignature(document, from);
    }

    private void addTrainingSessionForm(Document document, SessionAttendanceReportDto row, int formNumber) throws IOException {
        Long sessionId = row.getSessionId();
        String trainingIdDisplay = "Training ID: " + (sessionId != null ? sessionId : "—");
        addHeader(document, sessionId, trainingIdDisplay, row.getStartDateTime().toLocalDate());

        // Training Description (topics as bullet points)
        document.add(new Paragraph("Training Description:").setBold().setFontSize(11).setMarginTop(12).setMarginBottom(6));
        String topic = (row.getSessionTopic() != null && !row.getSessionTopic().isBlank()) ? row.getSessionTopic() : "—";
        List<String> topicBullets = Arrays.stream(topic.split("[,\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (topicBullets.isEmpty()) {
            topicBullets.add(topic);
        }
        for (String bullet : topicBullets) {
            document.add(new Paragraph("• " + bullet).setFontSize(10).setMarginLeft(10));
        }

        // Attendees
        document.add(new Paragraph("Attendees:").setBold().setFontSize(11).setMarginTop(10).setMarginBottom(4));
        String attendeesText = (row.getAttendedStudentNames() != null && !row.getAttendedStudentNames().isEmpty())
                ? "[" + String.join(", ", row.getAttendedStudentNames()) + "]"
                : "[]";
        document.add(new Paragraph(attendeesText).setFontSize(10));

        // Trained Provided by
        document.add(new Paragraph("Trained Provided by:").setBold().setFontSize(11).setMarginTop(8).setMarginBottom(4));
        String instructor = (row.getInstructorName() != null && !row.getInstructorName().isBlank()) ? row.getInstructorName() : "—";
        document.add(new Paragraph("[" + instructor + "]").setFontSize(10));

        // Training Method
        document.add(new Paragraph("Training Method:").setBold().setFontSize(11).setMarginTop(8).setMarginBottom(4));
        String method = (row.getSessionType() != null && !row.getSessionType().isBlank()) ? row.getSessionType() : "—";
        document.add(new Paragraph(method).setFontSize(10));

        // Note
        if (row.getNotes() != null && !row.getNotes().isBlank()) {
            document.add(new Paragraph("Note:").setBold().setFontSize(11).setMarginTop(8).setMarginBottom(4));
            document.add(new Paragraph(row.getNotes()).setFontSize(10));
        }

        // CFO's Signature
        document.add(new Paragraph("CFO's Signature:").setBold().setFontSize(11).setMarginTop(12).setMarginBottom(4));
        document.add(new Paragraph("_________________________").setFontSize(10).setMarginBottom(20));

        addGuidelinesAndSignature(document, row.getStartDateTime().toLocalDate(),
                row.getAttendedStudentSignatures() != null ? row.getAttendedStudentSignatures() : new ArrayList<>());
    }

    private void addHeader(Document document, Long sessionId, String trainingId, java.time.LocalDate trainingDate) throws IOException {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1})).useAllAvailableWidth();
        headerTable.setMarginBottom(15);

        // Left cell: DAB Logo (same logo as login/header: assets/DAB.png)
        Cell logoCell = new Cell().setBorder(Border.NO_BORDER);
        try {
            Resource logoResource = null;
            String[] logoPaths = {
                    "static/assets/DAB.png",
                    "classpath:static/assets/DAB.png",
                    "static/DAB.png",
                    "classpath:static/DAB.png",
                    "DAB.png",
                    "classpath:DAB.png"
            };
            for (String path : logoPaths) {
                try {
                    logoResource = new ClassPathResource(path);
                    if (logoResource.exists() && logoResource.isReadable()) {
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            if (logoResource != null && logoResource.exists()) {
                byte[] logoBytes = logoResource.getInputStream().readAllBytes();
                Image logo = new Image(ImageDataFactory.create(logoBytes));
                logo.setWidth(70);
                logoCell.add(logo);
            }
        } catch (Exception e) {
            logoCell.add(new Paragraph("Logo").setFontSize(10).setFontColor(new DeviceRgb(150, 150, 150)));
        }
        headerTable.addCell(logoCell);

        // Center cell: Da Afghanistan Bank + Training Date
        Cell centerCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        centerCell.add(new Paragraph("Da Afghanistan Bank").setBold().setFontSize(18).setFontColor(new DeviceRgb(211, 78, 78)));
        centerCell.add(new Paragraph("Training Date: " + trainingDate.format(DATE_FORMAT)).setFontSize(11).setMarginTop(4));
        headerTable.addCell(centerCell);

        // Right cell: Training ID
        Cell idCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        idCell.add(new Paragraph(trainingId).setFontSize(11).setBold());
        headerTable.addCell(idCell);

        document.add(headerTable);
    }

    private void addGuidelinesAndSignature(Document document, java.time.LocalDate date) {
        addGuidelinesAndSignature(document, date, new ArrayList<>());
    }

    private void addGuidelinesAndSignature(Document document, java.time.LocalDate date,
                                           List<AttendeeSignatureDto> signatures) {
        document.add(new Paragraph("_____________________________________________________")
                .setMarginTop(15).setMarginBottom(15));

        // CFO signature block (uses a shared CFO signature image when available)
        document.add(new Paragraph("CFO's Signature:").setBold().setFontSize(11).setMarginBottom(4));
        if (!addCfoSignatureImage(document)) {
            // Fallback line if CFO signature image is not configured
            document.add(new Paragraph("_________________________").setFontSize(10).setMarginBottom(8));
        } else {
            document.add(new Paragraph("").setMarginBottom(8));
        }

        document.add(new Paragraph("Guideline").setBold().setFontSize(12).setMarginBottom(8));
        String[] guidelines = {
                "Form must be filled accurately and completely for every training session.",
                "DAB Name, Training Date, Description, and Attendees' names must be filled without omission.",
                "Training Date should reflect the actual training date.",
                "Attendees' names should be selected from provided options, ensuring all are included.",
                "Trainer's signature is required at the end of each form.",
                "CFO's signature is required at the end, signifying approval.",
                "Appropriate authority must approve any modifications to form layout/content.",
                "Failure to adhere may result in disciplinary action."
        };
        for (int i = 0; i < guidelines.length; i++) {
            document.add(new Paragraph((i + 1) + ". " + guidelines[i]).setFontSize(9).setMarginBottom(3));
        }

        document.add(new Paragraph("\nBy signing below, I acknowledge that I have read and understood the guideline outlined above and agree to comply with its provisions.")
                .setFontSize(9).setMarginTop(12).setMarginBottom(8));

        document.add(new Paragraph("Signature of all participants:").setBold().setFontSize(10).setMarginBottom(4));
        if (signatures != null && !signatures.isEmpty()) {
            Table sigTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            sigTable.setFontSize(9);
            for (AttendeeSignatureDto sig : signatures) {
                sigTable.addCell(new Cell().add(new Paragraph(sig.getFullName() != null ? sig.getFullName() : "—")).setBorder(Border.NO_BORDER));
                Cell sigCell = new Cell().add(new Paragraph("—")).setBorder(Border.NO_BORDER);
                if (sig.getSignatureData() != null && !sig.getSignatureData().isBlank()) {
                    try {
                        String base64 = sig.getSignatureData();
                        if (base64.contains(",")) base64 = base64.substring(base64.indexOf(",") + 1).trim();
                        byte[] imgBytes = Base64.getDecoder().decode(base64);
                        if (imgBytes != null && imgBytes.length > 0) {
                            Image img = new Image(ImageDataFactory.create(imgBytes));
                            img.setWidth(100);
                            img.setHeight(40);
                            sigCell = new Cell().add(img).setBorder(Border.NO_BORDER);
                        }
                    } catch (Exception ignored) { }
                }
                sigTable.addCell(sigCell);
            }
            document.add(sigTable);
        } else {
            document.add(new Paragraph("_________________________").setFontSize(10).setMarginBottom(4));
        }
        document.add(new Paragraph("Date: " + date.format(DATE_FORMAT)).setFontSize(9).setMarginBottom(8));

        document.add(new Paragraph("Admin is Responsible to send a copy of this form to HR Training Center")
                .setFontSize(8).setFontColor(new DeviceRgb(100, 100, 100)).setTextAlignment(TextAlignment.RIGHT));
    }

    /**
     * Adds the CFO signature image to the document if a configured image is available on the classpath.
     * Returns true when an image was added, false when not found.
     */
    private boolean addCfoSignatureImage(Document document) {
        try {
            Resource sigResource = null;
            String[] sigPaths = {
                    "static/CFO_SIGNATURE.png",
                    "classpath:static/CFO_SIGNATURE.png",
                    "static/assets/CFO_SIGNATURE.png",
                    "classpath:static/assets/CFO_SIGNATURE.png",
                    "CFO_SIGNATURE.png",
                    "classpath:CFO_SIGNATURE.png"
            };

            for (String path : sigPaths) {
                try {
                    sigResource = new ClassPathResource(path);
                    if (sigResource.exists() && sigResource.isReadable()) {
                        break;
                    }
                } catch (Exception ignored) { }
            }

            if (sigResource != null && sigResource.exists()) {
                byte[] sigBytes = sigResource.getInputStream().readAllBytes();
                Image sigImg = new Image(ImageDataFactory.create(sigBytes));
                sigImg.setWidth(100);
                sigImg.setHeight(40);
                sigImg.setHorizontalAlignment(HorizontalAlignment.LEFT);
                document.add(sigImg);
                return true;
            }
        } catch (Exception ignored) {
            // If anything fails we simply fall back to the placeholder line
        }
        return false;
    }

    /** Comprehensive Single Session Report - student engagement, attendance, content coverage. */
    public byte[] generateSingleSessionReportPdf(SingleSessionReportDto report) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(40, 40, 40, 40);

        String trainingId = "Training ID: " + (report.getSessionId() != null ? report.getSessionId() : "—");
        addHeader(document, report.getSessionId(), trainingId, report.getStartDateTime().toLocalDate());

        document.add(new Paragraph("Comprehensive Session Report").setBold().setFontSize(14).setFontColor(new DeviceRgb(211, 78, 78)).setMarginBottom(12));

        // Session info
        document.add(new Paragraph("Program: " + (report.getProgramTitle() != null ? report.getProgramTitle() : "—")).setFontSize(10));
        document.add(new Paragraph("Instructor: " + (report.getInstructorName() != null ? report.getInstructorName() : "—")).setFontSize(10));
        document.add(new Paragraph("Method: " + (report.getSessionType() != null ? report.getSessionType() : "—")).setFontSize(10).setMarginBottom(12));

        // Content Coverage
        document.add(new Paragraph("Content Coverage:").setBold().setFontSize(11).setMarginBottom(6));
        if (report.getContentCoverage() != null) {
            for (String item : report.getContentCoverage()) {
                document.add(new Paragraph("• " + item).setFontSize(10).setMarginLeft(10));
            }
        }
        document.add(new Paragraph(""));

        // Attendance Summary
        document.add(new Paragraph("Attendance Summary").setBold().setFontSize(11).setMarginTop(10).setMarginBottom(6));
        document.add(new Paragraph(String.format("Present: %d | Absent: %d | Late: %d | Excused: %d | Total Enrolled: %d",
                report.getPresentCount(), report.getAbsentCount(), report.getLateCount(), report.getExcusedCount(), report.getTotalEnrolled()))
                .setFontSize(10).setMarginBottom(12));

        // Student Engagement Table
        document.add(new Paragraph("Student Engagement").setBold().setFontSize(11).setMarginBottom(6));
        if (report.getStudentEngagement() != null && !report.getStudentEngagement().isEmpty()) {
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 1.5f, 2, 1.5f})).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Student").setBold()).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Email").setBold()).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Notes").setBold()).setFontSize(9));
            table.addHeaderCell(new Cell().add(new Paragraph("Overall %").setBold()).setFontSize(9));
            for (StudentEngagementDto e : report.getStudentEngagement()) {
                table.addCell(new Cell().add(new Paragraph(e.getFullName() != null ? e.getFullName() : "—")).setFontSize(9));
                table.addCell(new Cell().add(new Paragraph(e.getEmail() != null ? e.getEmail() : "—")).setFontSize(9));
                table.addCell(new Cell().add(new Paragraph(e.getStatus() != null ? e.getStatus() : "—")).setFontSize(9));
                table.addCell(new Cell().add(new Paragraph(e.getNotes() != null ? e.getNotes() : "—")).setFontSize(9));
                table.addCell(new Cell().add(new Paragraph(e.getAttendancePercent() != null ? e.getAttendancePercent() + "%" : "—")).setFontSize(9));
            }
            document.add(table);
        }
        if (report.getNotes() != null && !report.getNotes().isBlank()) {
            document.add(new Paragraph("Note: " + report.getNotes()).setFontSize(9).setMarginTop(10));
        }
        List<AttendeeSignatureDto> sigs = report.getAttendedStudentSignatures() != null
                ? report.getAttendedStudentSignatures() : new ArrayList<>();
        addGuidelinesAndSignature(document, report.getStartDateTime().toLocalDate(), sigs);
        document.close();
        return baos.toByteArray();
    }

    /** Date-Based Grouped Report - by students and sessions, participation trends. */
    public byte[] generateDateBasedGroupedReportPdf(DateBasedGroupedReportDto report) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(40, 40, 40, 40);

        addHeader(document, null, "Consolidated Report", report.getFromDate());
        document.add(new Paragraph("Date-Based Grouped Report").setBold().setFontSize(14).setFontColor(new DeviceRgb(211, 78, 78)).setMarginBottom(4));
        document.add(new Paragraph("Period: " + report.getFromDate().format(DATE_FORMAT) + " — " + report.getToDate().format(DATE_FORMAT))
                .setFontSize(10).setMarginBottom(12));

        document.add(new Paragraph("Executive Summary").setBold().setFontSize(11).setMarginBottom(6));
        document.add(new Paragraph(String.format("Total Sessions: %d | Total Students: %d | Overall Participation Rate: %.1f%%",
                report.getTotalSessions(), report.getTotalStudents(), report.getOverallParticipationRate()))
                .setFontSize(10).setMarginBottom(16));

        // Section: By Student
        document.add(new Paragraph("Participation by Student").setBold().setFontSize(12).setMarginBottom(8));
        if (report.getByStudent() != null && !report.getByStudent().isEmpty()) {
            Table studentTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1.5f, 1.5f})).useAllAvailableWidth();
            studentTable.addHeaderCell(new Cell().add(new Paragraph("Student").setBold()).setFontSize(9));
            studentTable.addHeaderCell(new Cell().add(new Paragraph("Attended").setBold()).setFontSize(9));
            studentTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()).setFontSize(9));
            studentTable.addHeaderCell(new Cell().add(new Paragraph("Rate %").setBold()).setFontSize(9));
            studentTable.addHeaderCell(new Cell().add(new Paragraph("Trend").setBold()).setFontSize(9));
            for (StudentParticipationDto s : report.getByStudent()) {
                studentTable.addCell(new Cell().add(new Paragraph(s.getFullName() != null ? s.getFullName() : "—")).setFontSize(9));
                studentTable.addCell(new Cell().add(new Paragraph(String.valueOf(s.getSessionsAttended()))).setFontSize(9));
                studentTable.addCell(new Cell().add(new Paragraph(String.valueOf(s.getTotalSessions()))).setFontSize(9));
                studentTable.addCell(new Cell().add(new Paragraph(String.format("%.1f", s.getAttendancePercent()))).setFontSize(9));
                studentTable.addCell(new Cell().add(new Paragraph(s.getParticipationTrend() != null ? s.getParticipationTrend() : "—")).setFontSize(9));
            }
            document.add(studentTable);
        }
        document.add(new Paragraph(""));

        // Section: Session Coverage
        document.add(new Paragraph("Session Coverage").setBold().setFontSize(12).setMarginTop(12).setMarginBottom(8));
        List<AttendeeSignatureDto> allSignatures = new ArrayList<>();
        java.util.Set<Long> seenIds = new java.util.HashSet<>();
        if (report.getSessionsByDate() != null && !report.getSessionsByDate().isEmpty()) {
            for (SessionAttendanceReportDto s : report.getSessionsByDate()) {
                document.add(new Paragraph(s.getProgramTitle() + " — " + (s.getSessionTopic() != null ? s.getSessionTopic() : "—"))
                        .setFontSize(10).setBold());
                document.add(new Paragraph("Date: " + s.getStartDateTime().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) + " | Attendees: " +
                        (s.getAttendedStudentNames() != null ? String.join(", ", s.getAttendedStudentNames()) : "—"))
                        .setFontSize(9).setMarginBottom(6));
                if (s.getAttendedStudentSignatures() != null) {
                    for (AttendeeSignatureDto sig : s.getAttendedStudentSignatures()) {
                        if (sig != null && sig.getParticipantId() != null && !seenIds.contains(sig.getParticipantId())) {
                            seenIds.add(sig.getParticipantId());
                            allSignatures.add(sig);
                        }
                    }
                }
            }
        }
        addGuidelinesAndSignature(document, report.getToDate(), allSignatures);
        document.close();
        return baos.toByteArray();
    }
}

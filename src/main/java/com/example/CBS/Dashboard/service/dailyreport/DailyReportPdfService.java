package com.example.CBS.Dashboard.service.dailyreport;

import com.example.CBS.Dashboard.entity.DailyReport;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DailyReportPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public byte[] generateEmployeeReportPdf(List<DailyReport> reports) throws IOException {
        if (reports == null || reports.isEmpty()) {
            throw new IllegalArgumentException("No reports provided");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Set page margins
        document.setMargins(50, 50, 50, 50);

        // Header
        addHeader(document, reports.get(0));

        // Report sections
        for (DailyReport report : reports) {
            addReportContent(document, report);
            // Add page break between reports if multiple
            if (reports.indexOf(report) < reports.size() - 1) {
                document.add(new Paragraph("\n"));
            }
        }

        document.close();
        return baos.toByteArray();
    }

    public byte[] generateCombinedReportPdf(List<DailyReport> reports) throws IOException {
        if (reports == null || reports.isEmpty()) {
            throw new IllegalArgumentException("No reports provided");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.setMargins(50, 50, 50, 50);

        // Group by date
        java.util.Map<java.time.LocalDate, List<DailyReport>> reportsByDate = reports.stream()
            .collect(java.util.stream.Collectors.groupingBy(DailyReport::getBusinessDate));

        for (java.util.Map.Entry<java.time.LocalDate, List<DailyReport>> entry : reportsByDate.entrySet()) {
            addHeader(document, entry.getValue().get(0));
            document.add(new Paragraph("COMBINED TEAM REPORT - " + entry.getKey().format(DATE_FORMATTER))
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

            for (DailyReport report : entry.getValue()) {
                addReportContent(document, report);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document document, DailyReport report) throws IOException {
        // Title
        Paragraph title = new Paragraph("Da Afghanistan Bank")
            .setBold()
            .setFontSize(16)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5);
        document.add(title);

        Paragraph subtitle = new Paragraph("CBS TEAM DAILY STATUS REPORT")
            .setBold()
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(15);
        document.add(subtitle);

        // Report Info Table
        Table infoTable = new Table(2).useAllAvailableWidth();
        infoTable.setMarginBottom(10);

        addInfoRow(infoTable, "BUSINESS DAY:", report.getBusinessDate().format(DATE_FORMATTER));
        addInfoRow(infoTable, "Prepared By:", report.getEmployee().getUsername());
        if (report.getReviewedBy() != null) {
            addInfoRow(infoTable, "Reviewed By:", report.getReviewedBy().getUsername());
        }
        if (report.getReportingLine() != null && !report.getReportingLine().isEmpty()) {
            addInfoRow(infoTable, "Reporting Line:", report.getReportingLine());
        }
        if (report.getCbsEndTime() != null) {
            addInfoRow(infoTable, "CBS End Time:", report.getCbsEndTime().toString());
        }
        if (report.getCbsStartTimeNextDay() != null) {
            addInfoRow(infoTable, "CBS Start Time (Next Day):", report.getCbsStartTimeNextDay().toString());
        }

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
            .add(new Paragraph(label).setBold())
            .setPadding(5)
            .setBackgroundColor(ColorConstants.LIGHT_GRAY);
        Cell valueCell = new Cell()
            .add(new Paragraph(value != null ? value : ""))
            .setPadding(5);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addReportContent(Document document, DailyReport report) throws IOException {
        // CBS Team Activities
        if (report.getCbsTeamActivities() != null && !report.getCbsTeamActivities().isEmpty()) {
            document.add(new Paragraph("CBS Team Daily Activities (On-Job Activities)")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            int index = 1;
            for (var activity : report.getCbsTeamActivities()) {
                Paragraph activityPara = new Paragraph(index + ". " + activity.getDescription())
                    .setMarginBottom(3);
                document.add(activityPara);
                if (activity.getActivityType() != null && !activity.getActivityType().isEmpty()) {
                    document.add(new Paragraph("   Type: " + activity.getActivityType())
                        .setFontSize(10)
                        .setMarginBottom(2));
                }
                if (activity.getActionTaken() != null && !activity.getActionTaken().isEmpty()) {
                    document.add(new Paragraph("   Action Taken: " + activity.getActionTaken())
                        .setFontSize(10)
                        .setMarginBottom(2));
                }
                if (activity.getFinalStatus() != null && !activity.getFinalStatus().isEmpty()) {
                    document.add(new Paragraph("   Final Status: " + activity.getFinalStatus())
                        .setFontSize(10)
                        .setMarginBottom(2));
                }
                index++;
            }
            document.add(new Paragraph("\n"));
        }

        // Email Communications
        if (report.getEmailCommunications() != null && !report.getEmailCommunications().isEmpty()) {
            document.add(new Paragraph("Email Communication")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            Table emailTable = new Table(6).useAllAvailableWidth();
            emailTable.setMarginBottom(10);

            // Header
            addTableHeader(emailTable, "Internal/External");
            addTableHeader(emailTable, "Sender");
            addTableHeader(emailTable, "Receiver");
            addTableHeader(emailTable, "Subject");
            addTableHeader(emailTable, "Summary");
            addTableHeader(emailTable, "Action taken");

            // Rows
            for (var email : report.getEmailCommunications()) {
                addTableCell(emailTable, email.getIsInternal() ? "Internal" : "External");
                addTableCell(emailTable, email.getSender());
                addTableCell(emailTable, email.getReceiver());
                addTableCell(emailTable, email.getSubject());
                addTableCell(emailTable, email.getSummary());
                addTableCell(emailTable, email.getActionTaken() != null ? email.getActionTaken() : "");
            }

            document.add(emailTable);
            document.add(new Paragraph("\n"));
        }

        // Pending Activities
        if (report.getPendingActivities() != null && !report.getPendingActivities().isEmpty()) {
            document.add(new Paragraph("CBS Pending Activities")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            Table pendingTable = new Table(4).useAllAvailableWidth();
            pendingTable.setMarginBottom(10);

            addTableHeader(pendingTable, "Title");
            addTableHeader(pendingTable, "Description");
            addTableHeader(pendingTable, "Status");
            addTableHeader(pendingTable, "Amount");

            for (var pending : report.getPendingActivities()) {
                addTableCell(pendingTable, pending.getTitle());
                addTableCell(pendingTable, pending.getDescription());
                addTableCell(pendingTable, pending.getStatus());
                addTableCell(pendingTable, pending.getAmount() != null ? pending.getAmount().toString() : "");
            }

            document.add(pendingTable);
            document.add(new Paragraph("\n"));
        }

        // Chat Communications
        if (report.getChatCommunications() != null && !report.getChatCommunications().isEmpty()) {
            document.add(new Paragraph("Chat/Instant Messaging Communications")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            Table chatTable = new Table(4).useAllAvailableWidth();
            chatTable.setMarginBottom(10);

            addTableHeader(chatTable, "Platform");
            addTableHeader(chatTable, "Summary");
            addTableHeader(chatTable, "Action Taken");
            addTableHeader(chatTable, "Reference No.");

            for (var chat : report.getChatCommunications()) {
                addTableCell(chatTable, chat.getPlatform());
                addTableCell(chatTable, chat.getSummary());
                addTableCell(chatTable, chat.getActionTaken() != null ? chat.getActionTaken() : "");
                addTableCell(chatTable, chat.getReferenceNumber() != null ? chat.getReferenceNumber() : "");
            }

            document.add(chatTable);
            document.add(new Paragraph("\n"));
        }

        // Problem Escalations
        if (report.getProblemEscalations() != null && !report.getProblemEscalations().isEmpty()) {
            document.add(new Paragraph("Problem Escalation")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            for (var escalation : report.getProblemEscalations()) {
                document.add(new Paragraph("Escalated To: " + escalation.getEscalatedTo())
                    .setMarginBottom(2));
                document.add(new Paragraph("Reason: " + escalation.getReason())
                    .setMarginBottom(2));
                if (escalation.getFollowUpStatus() != null) {
                    document.add(new Paragraph("Follow-up Status: " + escalation.getFollowUpStatus())
                        .setMarginBottom(2));
                }
                document.add(new Paragraph("\n"));
            }
        }

        // Meetings
        if (report.getMeetings() != null && !report.getMeetings().isEmpty()) {
            document.add(new Paragraph("Meetings (Team Collaboration and External)")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            Table meetingTable = new Table(6).useAllAvailableWidth();
            meetingTable.setMarginBottom(10);

            addTableHeader(meetingTable, "Type");
            addTableHeader(meetingTable, "Topic");
            addTableHeader(meetingTable, "Summary");
            addTableHeader(meetingTable, "Action Taken");
            addTableHeader(meetingTable, "Next Step");
            addTableHeader(meetingTable, "Participants");

            for (var meeting : report.getMeetings()) {
                addTableCell(meetingTable, meeting.getMeetingType());
                addTableCell(meetingTable, meeting.getTopic());
                addTableCell(meetingTable, meeting.getSummary());
                addTableCell(meetingTable, meeting.getActionTaken() != null ? meeting.getActionTaken() : "");
                addTableCell(meetingTable, meeting.getNextStep() != null ? meeting.getNextStep() : "");
                addTableCell(meetingTable, meeting.getParticipants() != null ? meeting.getParticipants() : "");
            }

            document.add(meetingTable);
            document.add(new Paragraph("\n"));
        }

        // AFPay Card Requests
        if (report.getAfpayCardRequests() != null && !report.getAfpayCardRequests().isEmpty()) {
            document.add(new Paragraph("AFPay Card")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            Table afpayTable = new Table(6).useAllAvailableWidth();
            afpayTable.setMarginBottom(10);

            addTableHeader(afpayTable, "Type");
            addTableHeader(afpayTable, "Requested By");
            addTableHeader(afpayTable, "Request date");
            addTableHeader(afpayTable, "Resolution");
            addTableHeader(afpayTable, "Support Doc Scanned");
            addTableHeader(afpayTable, "Date Archived");

            for (var afpay : report.getAfpayCardRequests()) {
                addTableCell(afpayTable, afpay.getRequestType());
                addTableCell(afpayTable, afpay.getRequestedBy());
                addTableCell(afpayTable, afpay.getRequestDate() != null ? afpay.getRequestDate().toString() : "");
                addTableCell(afpayTable, afpay.getResolutionDetails() != null ? afpay.getResolutionDetails() : "");
                addTableCell(afpayTable, afpay.getSupportingDocumentPath() != null ? afpay.getSupportingDocumentPath() : "");
                addTableCell(afpayTable, afpay.getArchivedDate() != null ? afpay.getArchivedDate().toString() : "");
            }

            document.add(afpayTable);
            document.add(new Paragraph("\n"));
        }

        // QRMIS Issues
        if (report.getQrmisIssues() != null && !report.getQrmisIssues().isEmpty()) {
            document.add(new Paragraph("QRMIS Issues / Tickets")
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(5));

            Table qrmisTable = new Table(7).useAllAvailableWidth();
            qrmisTable.setMarginBottom(10);

            addTableHeader(qrmisTable, "Problem Type");
            addTableHeader(qrmisTable, "Problem Description");
            addTableHeader(qrmisTable, "Solution provided");
            addTableHeader(qrmisTable, "Posted by");
            addTableHeader(qrmisTable, "Authorized By");
            addTableHeader(qrmisTable, "Support Document");
            addTableHeader(qrmisTable, "Operator");

            for (var qrmis : report.getQrmisIssues()) {
                addTableCell(qrmisTable, qrmis.getProblemType());
                addTableCell(qrmisTable, qrmis.getProblemDescription());
                addTableCell(qrmisTable, qrmis.getSolutionProvided() != null ? qrmis.getSolutionProvided() : "");
                addTableCell(qrmisTable, qrmis.getPostedBy() != null ? qrmis.getPostedBy() : "");
                addTableCell(qrmisTable, qrmis.getAuthorizedBy() != null ? qrmis.getAuthorizedBy() : "");
                addTableCell(qrmisTable, qrmis.getSupportingDocumentsArchived() != null ? qrmis.getSupportingDocumentsArchived() : "");
                addTableCell(qrmisTable, qrmis.getOperator() != null ? qrmis.getOperator() : "");
            }

            document.add(qrmisTable);
        }
    }

    private void addTableHeader(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text).setBold())
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setPadding(5)
            .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell);
    }

    private void addTableCell(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text != null ? text : ""))
            .setPadding(5);
        table.addCell(cell);
    }
}


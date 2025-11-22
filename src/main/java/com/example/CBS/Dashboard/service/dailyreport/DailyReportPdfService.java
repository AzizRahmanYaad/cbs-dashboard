package com.example.CBS.Dashboard.service.dailyreport;

import com.example.CBS.Dashboard.entity.DailyReport;
import com.example.CBS.Dashboard.entity.CbsTeamActivity;
import com.example.CBS.Dashboard.entity.ChatCommunication;
import com.example.CBS.Dashboard.entity.EmailCommunication;
import com.example.CBS.Dashboard.entity.ProblemEscalation;
import com.example.CBS.Dashboard.entity.PendingActivity;
import com.example.CBS.Dashboard.entity.Meeting;
import com.example.CBS.Dashboard.entity.AfpayCardRequest;
import com.example.CBS.Dashboard.entity.QrmisIssue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public byte[] generateCombinedReportPdf(List<DailyReport> reports, java.time.LocalTime cbsEndTime, java.time.LocalTime cbsStartTimeNextDay) throws IOException {
        if (reports == null || reports.isEmpty()) {
            throw new IllegalArgumentException("No reports provided");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.setMargins(40, 40, 40, 40);

        // All reports should be for the same date (enforced by service)
        java.time.LocalDate businessDate = reports.get(0).getBusinessDate();
        
        // Add main header once with CBS time tracking
        addCombinedHeader(document, reports.get(0), businessDate, cbsEndTime, cbsStartTimeNextDay);
        
        // Merge all activities from all employees for this single date
        addCombinedReportContent(document, reports);

        document.close();
        return baos.toByteArray();
    }
    
    private void addCombinedHeader(Document document, DailyReport sampleReport, java.time.LocalDate businessDate, 
                                   java.time.LocalTime cbsEndTime, java.time.LocalTime cbsStartTimeNextDay) {
        // Main Title with brown/gold accent
        Paragraph mainTitle = new Paragraph("Da Afghanistan Bank")
            .setBold()
            .setFontSize(20)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5)
            .setFontColor(new DeviceRgb(139, 69, 19)); // Brown color
        document.add(mainTitle);

        Paragraph subtitle = new Paragraph("CBS TEAM DAILY STATUS REPORT")
            .setBold()
            .setFontSize(17)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(8)
            .setFontColor(new DeviceRgb(101, 67, 33)); // Darker brown
        document.add(subtitle);

        Paragraph businessDay = new Paragraph("BUSINESS DAY: " + businessDate.format(DATE_FORMATTER))
            .setBold()
            .setFontSize(13)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(15)
            .setFontColor(new DeviceRgb(139, 69, 19)) // Brown
            .setBackgroundColor(new DeviceRgb(245, 222, 179)) // Light brown background
            .setPadding(8);
        document.add(businessDay);

        // Report Info Table with better styling (brown theme)
        Table infoTable = new Table(2).useAllAvailableWidth();
        infoTable.setMarginBottom(15);
        infoTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledInfoRow(infoTable, "Prepared By:", getEmployeeNames(sampleReport));
        if (sampleReport.getReportingLine() != null && !sampleReport.getReportingLine().isEmpty()) {
            addStyledInfoRow(infoTable, "Reporting Line:", sampleReport.getReportingLine());
        }
        // Add CBS Time Tracking from Controller input
        if (cbsEndTime != null) {
            addStyledInfoRow(infoTable, "CBS End Time (Current Business Day):", cbsEndTime.toString());
        }
        if (cbsStartTimeNextDay != null) {
            addStyledInfoRow(infoTable, "CBS Start Time (Next Business Day):", cbsStartTimeNextDay.toString());
        }

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }
    
    private String getEmployeeNames(DailyReport report) {
        // This will be called with a sample report, but we'll collect all names in the content section
        return "Team Members";
    }
    
    private void addStyledInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
            .add(new Paragraph(label).setBold().setFontSize(10))
            .setPadding(8)
            .setBackgroundColor(new DeviceRgb(222, 184, 135)) // Burlywood brown
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown text
            .setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 0.5f));
        Cell valueCell = new Cell()
            .add(new Paragraph(value != null ? value : "").setFontSize(10))
            .setPadding(8)
            .setBackgroundColor(new DeviceRgb(255, 248, 220)) // Cornsilk
            .setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 0.5f));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private void addCombinedReportContent(Document document, List<DailyReport> reports) {
        // Collect all activities from all employees
        List<ActivityWithEmployee> allActivities = new ArrayList<>();
        List<ActivityWithEmployee> withoutCheckNumberActivities = new ArrayList<>();
        List<EmailCommunication> allEmails = new ArrayList<>();
        List<ChatCommunication> allChats = new ArrayList<>();
        List<PendingActivity> allPending = new ArrayList<>();
        List<ProblemEscalation> allEscalations = new ArrayList<>();
        List<Meeting> allMeetings = new ArrayList<>();
        List<AfpayCardRequest> allAfpay = new ArrayList<>();
        List<QrmisIssue> allQrmis = new ArrayList<>();
        
        for (DailyReport report : reports) {
            String employeeName = report.getEmployee().getUsername();
            
            // Collect CBS Team Activities with employee name
            if (report.getCbsTeamActivities() != null) {
                for (CbsTeamActivity activity : report.getCbsTeamActivities()) {
                    // Separate "without check number" activities
                    if (activity.getActivityType() != null && 
                        (activity.getActivityType().toLowerCase().contains("without check number") ||
                         activity.getActivityType().toLowerCase().contains("allowing without check number"))) {
                        withoutCheckNumberActivities.add(new ActivityWithEmployee(activity, employeeName));
                    } else {
                        allActivities.add(new ActivityWithEmployee(activity, employeeName));
                    }
                }
            }
            
            // Collect other sections
            if (report.getEmailCommunications() != null) {
                allEmails.addAll(report.getEmailCommunications());
            }
            if (report.getChatCommunications() != null) {
                allChats.addAll(report.getChatCommunications());
            }
            if (report.getPendingActivities() != null) {
                allPending.addAll(report.getPendingActivities());
            }
            if (report.getProblemEscalations() != null) {
                allEscalations.addAll(report.getProblemEscalations());
            }
            if (report.getMeetings() != null) {
                allMeetings.addAll(report.getMeetings());
            }
            if (report.getAfpayCardRequests() != null) {
                allAfpay.addAll(report.getAfpayCardRequests());
            }
            if (report.getQrmisIssues() != null) {
                allQrmis.addAll(report.getQrmisIssues());
            }
        }
        
        // Add "Without Check Number" section first (as a separate table)
        if (!withoutCheckNumberActivities.isEmpty()) {
            addWithoutCheckNumberSection(document, withoutCheckNumberActivities);
        }
        
        // Add merged CBS Team Activities section (excluding without check number)
        if (!allActivities.isEmpty()) {
            addMergedCbsActivitiesSection(document, allActivities);
        }
        
        // Add other merged sections
        if (!allEmails.isEmpty()) {
            addMergedEmailSection(document, allEmails);
        }
        
        if (!allChats.isEmpty()) {
            addMergedChatSection(document, allChats);
        }
        
        if (!allPending.isEmpty()) {
            addMergedPendingSection(document, allPending);
        }
        
        if (!allEscalations.isEmpty()) {
            addMergedEscalationSection(document, allEscalations);
        }
        
        if (!allMeetings.isEmpty()) {
            addMergedMeetingSection(document, allMeetings);
        }
        
        if (!allAfpay.isEmpty()) {
            addMergedAfpaySection(document, allAfpay);
        }
        
        if (!allQrmis.isEmpty()) {
            addMergedQrmisSection(document, allQrmis);
        }
    }
    
    private void addWithoutCheckNumberSection(Document document, List<ActivityWithEmployee> activities) {
        Paragraph sectionTitle = new Paragraph("Allowing Without Check Number")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(12)
            .setFontColor(new DeviceRgb(139, 69, 19)) // Brown
            .setBackgroundColor(new DeviceRgb(245, 222, 179)) // Light brown background
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);
        
        // Create a professional table
        Table withoutCheckTable = new Table(5).useAllAvailableWidth();
        withoutCheckTable.setMarginBottom(15);
        withoutCheckTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));
        
        // Table headers with brown styling
        addStyledTableHeaderBrown(withoutCheckTable, "No.");
        addStyledTableHeaderBrown(withoutCheckTable, "Branch");
        addStyledTableHeaderBrown(withoutCheckTable, "Account Number");
        addStyledTableHeaderBrown(withoutCheckTable, "Description");
        addStyledTableHeaderBrown(withoutCheckTable, "Employee");
        
        int index = 1;
        for (ActivityWithEmployee item : activities) {
            addStyledTableCellBrown(withoutCheckTable, String.valueOf(index));
            addStyledTableCellBrown(withoutCheckTable, item.activity.getBranch() != null ? item.activity.getBranch() : "");
            addStyledTableCellBrown(withoutCheckTable, item.activity.getAccountNumber() != null ? item.activity.getAccountNumber() : "");
            addStyledTableCellBrown(withoutCheckTable, item.activity.getDescription());
            addStyledTableCellBrown(withoutCheckTable, item.employeeName);
            index++;
        }
        
        document.add(withoutCheckTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedCbsActivitiesSection(Document document, List<ActivityWithEmployee> activities) {
        Paragraph sectionTitle = new Paragraph("CBS Team Daily Activities (On-Job Activities)")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(139, 69, 19)) // Brown
            .setBackgroundColor(new DeviceRgb(245, 222, 179)) // Light brown background
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);
        
        String currentEmployee = null;
        int index = 1;
        
        for (ActivityWithEmployee item : activities) {
            // If employee changed, add separator
            if (currentEmployee != null && !currentEmployee.equals(item.employeeName)) {
                document.add(new Paragraph("")
                    .setMarginBottom(5));
            }
            
            // Add activity
            Paragraph activityPara = new Paragraph(index + ". " + item.activity.getDescription())
                .setFontSize(11)
                .setMarginBottom(3)
                .setMarginLeft(10);
            document.add(activityPara);
            
            if (item.activity.getActivityType() != null && !item.activity.getActivityType().isEmpty()) {
                document.add(new Paragraph("   Type: " + item.activity.getActivityType())
                    .setFontSize(10)
                    .setMarginLeft(15)
                    .setMarginBottom(2)
                    .setFontColor(new DeviceRgb(80, 80, 80)));
            }
            if (item.activity.getActionTaken() != null && !item.activity.getActionTaken().isEmpty()) {
                document.add(new Paragraph("   Action Taken: " + item.activity.getActionTaken())
                    .setFontSize(10)
                    .setMarginLeft(15)
                    .setMarginBottom(2)
                    .setFontColor(new DeviceRgb(80, 80, 80)));
            }
            if (item.activity.getFinalStatus() != null && !item.activity.getFinalStatus().isEmpty()) {
                document.add(new Paragraph("   Final Status: " + item.activity.getFinalStatus())
                    .setFontSize(10)
                    .setMarginLeft(15)
                    .setMarginBottom(2)
                    .setFontColor(new DeviceRgb(80, 80, 80)));
            }
            
            // Check if next activity is from different employee
            boolean isLast = activities.indexOf(item) == activities.size() - 1;
            boolean nextIsDifferent = !isLast && 
                !item.employeeName.equals(activities.get(activities.indexOf(item) + 1).employeeName);
            
            if (isLast || nextIsDifferent) {
                // Add employee name in bold with brown color
                document.add(new Paragraph("   — " + item.employeeName)
                    .setBold()
                    .setFontSize(10)
                    .setMarginLeft(15)
                    .setMarginBottom(8)
                    .setFontColor(new DeviceRgb(139, 69, 19))); // Brown
            }
            
            currentEmployee = item.employeeName;
            index++;
        }
        
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedEmailSection(Document document, List<EmailCommunication> emails) {
        Paragraph sectionTitle = new Paragraph("Email Communication")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

        Table emailTable = new Table(6).useAllAvailableWidth();
        emailTable.setMarginBottom(10);
        emailTable.setBorder(new SolidBorder(new DeviceRgb(0, 51, 102), 1));

        addStyledTableHeader(emailTable, "Internal/External");
        addStyledTableHeader(emailTable, "Sender");
        addStyledTableHeader(emailTable, "Receiver");
        addStyledTableHeader(emailTable, "Subject");
        addStyledTableHeader(emailTable, "Summary");
        addStyledTableHeader(emailTable, "Action taken");

        for (EmailCommunication email : emails) {
            addStyledTableCell(emailTable, email.getIsInternal() ? "Internal" : "External");
            addStyledTableCell(emailTable, email.getSender());
            addStyledTableCell(emailTable, email.getReceiver());
            addStyledTableCell(emailTable, email.getSubject());
            addStyledTableCell(emailTable, email.getSummary());
            addStyledTableCell(emailTable, email.getActionTaken() != null ? email.getActionTaken() : "");
        }

        document.add(emailTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedChatSection(Document document, List<ChatCommunication> chats) {
        Paragraph sectionTitle = new Paragraph("Chat/Instant Messaging Communications")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

            Table chatTable = new Table(4).useAllAvailableWidth();
            chatTable.setMarginBottom(10);
            chatTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledTableHeader(chatTable, "Platform");
        addStyledTableHeader(chatTable, "Summary");
        addStyledTableHeader(chatTable, "Action Taken");
        addStyledTableHeader(chatTable, "Reference No.");

        for (ChatCommunication chat : chats) {
            addStyledTableCell(chatTable, chat.getPlatform());
            addStyledTableCell(chatTable, chat.getSummary());
            addStyledTableCell(chatTable, chat.getActionTaken() != null ? chat.getActionTaken() : "");
            addStyledTableCell(chatTable, chat.getReferenceNumber() != null ? chat.getReferenceNumber() : "");
        }

        document.add(chatTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedPendingSection(Document document, List<PendingActivity> pending) {
        Paragraph sectionTitle = new Paragraph("CBS Pending Activities")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

            Table pendingTable = new Table(4).useAllAvailableWidth();
            pendingTable.setMarginBottom(10);
            pendingTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledTableHeader(pendingTable, "Title");
        addStyledTableHeader(pendingTable, "Description");
        addStyledTableHeader(pendingTable, "Status");
        addStyledTableHeader(pendingTable, "Amount");

        for (PendingActivity item : pending) {
            addStyledTableCell(pendingTable, item.getTitle());
            addStyledTableCell(pendingTable, item.getDescription());
            addStyledTableCell(pendingTable, item.getStatus());
            addStyledTableCell(pendingTable, item.getAmount() != null ? item.getAmount().toString() : "");
        }

        document.add(pendingTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedEscalationSection(Document document, List<ProblemEscalation> escalations) {
        Paragraph sectionTitle = new Paragraph("Problem Escalation")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

        for (ProblemEscalation escalation : escalations) {
            document.add(new Paragraph("Escalated To: " + escalation.getEscalatedTo())
                .setFontSize(11)
                .setMarginBottom(3)
                .setMarginLeft(10));
            document.add(new Paragraph("Reason: " + escalation.getReason())
                .setFontSize(11)
                .setMarginBottom(3)
                .setMarginLeft(10));
            if (escalation.getFollowUpStatus() != null) {
                document.add(new Paragraph("Follow-up Status: " + escalation.getFollowUpStatus())
                    .setFontSize(11)
                    .setMarginBottom(5)
                    .setMarginLeft(10));
            }
            document.add(new Paragraph("\n"));
        }
    }
    
    private void addMergedMeetingSection(Document document, List<Meeting> meetings) {
        Paragraph sectionTitle = new Paragraph("Meetings (Team Collaboration and External)")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

            Table meetingTable = new Table(6).useAllAvailableWidth();
            meetingTable.setMarginBottom(10);
            meetingTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledTableHeader(meetingTable, "Type");
        addStyledTableHeader(meetingTable, "Topic");
        addStyledTableHeader(meetingTable, "Summary");
        addStyledTableHeader(meetingTable, "Action Taken");
        addStyledTableHeader(meetingTable, "Next Step");
        addStyledTableHeader(meetingTable, "Participants");

        for (Meeting meeting : meetings) {
            addStyledTableCell(meetingTable, meeting.getMeetingType());
            addStyledTableCell(meetingTable, meeting.getTopic());
            addStyledTableCell(meetingTable, meeting.getSummary());
            addStyledTableCell(meetingTable, meeting.getActionTaken() != null ? meeting.getActionTaken() : "");
            addStyledTableCell(meetingTable, meeting.getNextStep() != null ? meeting.getNextStep() : "");
            addStyledTableCell(meetingTable, meeting.getParticipants() != null ? meeting.getParticipants() : "");
        }

        document.add(meetingTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedAfpaySection(Document document, List<AfpayCardRequest> afpay) {
        Paragraph sectionTitle = new Paragraph("AFPay Card")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

            Table afpayTable = new Table(6).useAllAvailableWidth();
            afpayTable.setMarginBottom(10);
            afpayTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledTableHeader(afpayTable, "Type");
        addStyledTableHeader(afpayTable, "Requested By");
        addStyledTableHeader(afpayTable, "Request date");
        addStyledTableHeader(afpayTable, "Resolution");
        addStyledTableHeader(afpayTable, "Support Doc Scanned");
        addStyledTableHeader(afpayTable, "Date Archived");

        for (AfpayCardRequest item : afpay) {
            addStyledTableCell(afpayTable, item.getRequestType());
            addStyledTableCell(afpayTable, item.getRequestedBy());
            addStyledTableCell(afpayTable, item.getRequestDate() != null ? item.getRequestDate().toString() : "");
            addStyledTableCell(afpayTable, item.getResolutionDetails() != null ? item.getResolutionDetails() : "");
            addStyledTableCell(afpayTable, item.getSupportingDocumentPath() != null ? item.getSupportingDocumentPath() : "");
            addStyledTableCell(afpayTable, item.getArchivedDate() != null ? item.getArchivedDate().toString() : "");
        }

        document.add(afpayTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addMergedQrmisSection(Document document, List<QrmisIssue> qrmis) {
        Paragraph sectionTitle = new Paragraph("QRMIS Issues / Tickets")
            .setBold()
            .setFontSize(14)
            .setMarginTop(15)
            .setMarginBottom(10)
            .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
            .setPadding(10)
            .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
        document.add(sectionTitle);

            Table qrmisTable = new Table(7).useAllAvailableWidth();
            qrmisTable.setMarginBottom(10);
            qrmisTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledTableHeader(qrmisTable, "Problem Type");
        addStyledTableHeader(qrmisTable, "Problem Description");
        addStyledTableHeader(qrmisTable, "Solution provided");
        addStyledTableHeader(qrmisTable, "Posted by");
        addStyledTableHeader(qrmisTable, "Authorized By");
        addStyledTableHeader(qrmisTable, "Support Document");
        addStyledTableHeader(qrmisTable, "Operator");

        for (QrmisIssue item : qrmis) {
            addStyledTableCell(qrmisTable, item.getProblemType());
            addStyledTableCell(qrmisTable, item.getProblemDescription());
            addStyledTableCell(qrmisTable, item.getSolutionProvided() != null ? item.getSolutionProvided() : "");
            addStyledTableCell(qrmisTable, item.getPostedBy() != null ? item.getPostedBy() : "");
            addStyledTableCell(qrmisTable, item.getAuthorizedBy() != null ? item.getAuthorizedBy() : "");
            addStyledTableCell(qrmisTable, item.getSupportingDocumentsArchived() != null ? item.getSupportingDocumentsArchived() : "");
            addStyledTableCell(qrmisTable, item.getOperator() != null ? item.getOperator() : "");
        }

        document.add(qrmisTable);
    }
    
    private void addStyledTableHeader(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text).setBold().setFontSize(10))
            .setBackgroundColor(new DeviceRgb(139, 69, 19)) // Brown
            .setFontColor(ColorConstants.WHITE)
            .setPadding(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(new SolidBorder(new DeviceRgb(101, 67, 33), 0.5f));
        table.addHeaderCell(cell);
    }
    
    private void addStyledTableHeaderBrown(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text).setBold().setFontSize(11))
            .setBackgroundColor(new DeviceRgb(101, 67, 33)) // Dark brown
            .setFontColor(ColorConstants.WHITE)
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 1));
        table.addHeaderCell(cell);
    }

    private void addStyledTableCell(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text != null ? text : "").setFontSize(9))
            .setPadding(6)
            .setBackgroundColor(new DeviceRgb(255, 248, 220)) // Cornsilk
            .setBorder(new SolidBorder(new DeviceRgb(222, 184, 135), 0.5f));
        table.addCell(cell);
    }
    
    private void addStyledTableCellBrown(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text != null ? text : "").setFontSize(10))
            .setPadding(8)
            .setBackgroundColor(new DeviceRgb(255, 250, 240)) // Floral white
            .setBorder(new SolidBorder(new DeviceRgb(222, 184, 135), 0.5f));
        table.addCell(cell);
    }
    
    // Helper class to track activities with employee names
    private static class ActivityWithEmployee {
        CbsTeamActivity activity;
        String employeeName;
        
        ActivityWithEmployee(CbsTeamActivity activity, String employeeName) {
            this.activity = activity;
            this.employeeName = employeeName;
        }
    }

    private void addHeader(Document document, DailyReport report) throws IOException {
        // Title with professional brown styling
        Paragraph title = new Paragraph("Da Afghanistan Bank")
            .setBold()
            .setFontSize(20)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5)
            .setFontColor(new DeviceRgb(139, 69, 19)); // Brown
        document.add(title);

        Paragraph subtitle = new Paragraph("CBS TEAM DAILY STATUS REPORT")
            .setBold()
            .setFontSize(17)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(8)
            .setFontColor(new DeviceRgb(101, 67, 33)); // Darker brown
        document.add(subtitle);

        Paragraph businessDay = new Paragraph("BUSINESS DAY: " + report.getBusinessDate().format(DATE_FORMATTER))
            .setBold()
            .setFontSize(13)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(15)
            .setFontColor(new DeviceRgb(139, 69, 19)) // Brown
            .setBackgroundColor(new DeviceRgb(245, 222, 179)) // Light brown background
            .setPadding(8);
        document.add(businessDay);

        // Report Info Table with professional brown styling
        Table infoTable = new Table(2).useAllAvailableWidth();
        infoTable.setMarginBottom(15);
        infoTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

        addStyledInfoRow(infoTable, "Prepared By:", report.getEmployee().getUsername());
        if (report.getReviewedBy() != null) {
            addStyledInfoRow(infoTable, "Reviewed By:", report.getReviewedBy().getUsername());
        }
        if (report.getReportingLine() != null && !report.getReportingLine().isEmpty()) {
            addStyledInfoRow(infoTable, "Reporting Line:", report.getReportingLine());
        }
        if (report.getCbsEndTime() != null) {
            addStyledInfoRow(infoTable, "CBS End Time:", report.getCbsEndTime().toString());
        }
        if (report.getCbsStartTimeNextDay() != null) {
            addStyledInfoRow(infoTable, "CBS Start Time (Next Day):", report.getCbsStartTimeNextDay().toString());
        }

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addReportContent(Document document, DailyReport report) throws IOException {
        // Separate "without check number" activities first
        List<CbsTeamActivity> withoutCheckNumber = new ArrayList<>();
        List<CbsTeamActivity> otherActivities = new ArrayList<>();
        
        if (report.getCbsTeamActivities() != null && !report.getCbsTeamActivities().isEmpty()) {
            for (CbsTeamActivity activity : report.getCbsTeamActivities()) {
                if (activity.getActivityType() != null && 
                    (activity.getActivityType().toLowerCase().contains("without check number") ||
                     activity.getActivityType().toLowerCase().contains("allowing without check number"))) {
                    withoutCheckNumber.add(activity);
                } else {
                    otherActivities.add(activity);
                }
            }
        }
        
        // Add "Without Check Number" section first
        if (!withoutCheckNumber.isEmpty()) {
            Paragraph sectionTitle = new Paragraph("Allowing Without Check Number")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(12)
                .setFontColor(new DeviceRgb(139, 69, 19)) // Brown
                .setBackgroundColor(new DeviceRgb(245, 222, 179)) // Light brown background
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);
            
            Table withoutCheckTable = new Table(4).useAllAvailableWidth();
            withoutCheckTable.setMarginBottom(15);
            withoutCheckTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));
            
            addStyledTableHeaderBrown(withoutCheckTable, "No.");
            addStyledTableHeaderBrown(withoutCheckTable, "Branch");
            addStyledTableHeaderBrown(withoutCheckTable, "Account Number");
            addStyledTableHeaderBrown(withoutCheckTable, "Description");
            
            int index = 1;
            for (CbsTeamActivity activity : withoutCheckNumber) {
                addStyledTableCellBrown(withoutCheckTable, String.valueOf(index));
                addStyledTableCellBrown(withoutCheckTable, activity.getBranch() != null ? activity.getBranch() : "");
                addStyledTableCellBrown(withoutCheckTable, activity.getAccountNumber() != null ? activity.getAccountNumber() : "");
                addStyledTableCellBrown(withoutCheckTable, activity.getDescription());
                index++;
            }
            
            document.add(withoutCheckTable);
            document.add(new Paragraph("\n"));
        }
        
        // CBS Team Activities (excluding without check number)
        if (!otherActivities.isEmpty()) {
            Paragraph sectionTitle = new Paragraph("CBS Team Daily Activities (On-Job Activities)")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(139, 69, 19)) // Brown
                .setBackgroundColor(new DeviceRgb(245, 222, 179)) // Light brown background
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            int index = 1;
            for (var activity : otherActivities) {
                Paragraph activityPara = new Paragraph(index + ". " + activity.getDescription())
                    .setFontSize(11)
                    .setMarginBottom(3)
                    .setMarginLeft(10);
                document.add(activityPara);
                if (activity.getActivityType() != null && !activity.getActivityType().isEmpty()) {
                    document.add(new Paragraph("   Type: " + activity.getActivityType())
                        .setFontSize(10)
                        .setMarginLeft(15)
                        .setMarginBottom(2)
                        .setFontColor(new DeviceRgb(101, 67, 33))); // Brown
                }
                if (activity.getActionTaken() != null && !activity.getActionTaken().isEmpty()) {
                    document.add(new Paragraph("   Action Taken: " + activity.getActionTaken())
                        .setFontSize(10)
                        .setMarginLeft(15)
                        .setMarginBottom(2)
                        .setFontColor(new DeviceRgb(101, 67, 33))); // Brown
                }
                if (activity.getFinalStatus() != null && !activity.getFinalStatus().isEmpty()) {
                    document.add(new Paragraph("   Final Status: " + activity.getFinalStatus())
                        .setFontSize(10)
                        .setMarginLeft(15)
                        .setMarginBottom(2)
                        .setFontColor(new DeviceRgb(101, 67, 33))); // Brown
                }
                index++;
            }
            document.add(new Paragraph("\n"));
        }

        // Email Communications
        if (report.getEmailCommunications() != null && !report.getEmailCommunications().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("Email Communication")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            Table emailTable = new Table(6).useAllAvailableWidth();
            emailTable.setMarginBottom(10);
            emailTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

            // Header
            addStyledTableHeader(emailTable, "Internal/External");
            addStyledTableHeader(emailTable, "Sender");
            addStyledTableHeader(emailTable, "Receiver");
            addStyledTableHeader(emailTable, "Subject");
            addStyledTableHeader(emailTable, "Summary");
            addStyledTableHeader(emailTable, "Action taken");

            // Rows
            for (var email : report.getEmailCommunications()) {
                addStyledTableCell(emailTable, email.getIsInternal() ? "Internal" : "External");
                addStyledTableCell(emailTable, email.getSender());
                addStyledTableCell(emailTable, email.getReceiver());
                addStyledTableCell(emailTable, email.getSubject());
                addStyledTableCell(emailTable, email.getSummary());
                addStyledTableCell(emailTable, email.getActionTaken() != null ? email.getActionTaken() : "");
            }

            document.add(emailTable);
            document.add(new Paragraph("\n"));
        }

        // Pending Activities
        if (report.getPendingActivities() != null && !report.getPendingActivities().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("CBS Pending Activities")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            Table pendingTable = new Table(4).useAllAvailableWidth();
            pendingTable.setMarginBottom(10);
            pendingTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

            addStyledTableHeader(pendingTable, "Title");
            addStyledTableHeader(pendingTable, "Description");
            addStyledTableHeader(pendingTable, "Status");
            addStyledTableHeader(pendingTable, "Amount");

            for (var pending : report.getPendingActivities()) {
                addStyledTableCell(pendingTable, pending.getTitle());
                addStyledTableCell(pendingTable, pending.getDescription());
                addStyledTableCell(pendingTable, pending.getStatus());
                addStyledTableCell(pendingTable, pending.getAmount() != null ? pending.getAmount().toString() : "");
            }

            document.add(pendingTable);
            document.add(new Paragraph("\n"));
        }

        // Chat Communications
        if (report.getChatCommunications() != null && !report.getChatCommunications().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("Chat/Instant Messaging Communications")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            Table chatTable = new Table(4).useAllAvailableWidth();
            chatTable.setMarginBottom(10);
            chatTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

            addStyledTableHeader(chatTable, "Platform");
            addStyledTableHeader(chatTable, "Summary");
            addStyledTableHeader(chatTable, "Action Taken");
            addStyledTableHeader(chatTable, "Reference No.");

            for (var chat : report.getChatCommunications()) {
                addStyledTableCell(chatTable, chat.getPlatform());
                addStyledTableCell(chatTable, chat.getSummary());
                addStyledTableCell(chatTable, chat.getActionTaken() != null ? chat.getActionTaken() : "");
                addStyledTableCell(chatTable, chat.getReferenceNumber() != null ? chat.getReferenceNumber() : "");
            }

            document.add(chatTable);
            document.add(new Paragraph("\n"));
        }

        // Problem Escalations
        if (report.getProblemEscalations() != null && !report.getProblemEscalations().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("Problem Escalation")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            for (var escalation : report.getProblemEscalations()) {
                document.add(new Paragraph("Escalated To: " + escalation.getEscalatedTo())
                    .setFontSize(11)
                    .setMarginBottom(3)
                    .setMarginLeft(10));
                document.add(new Paragraph("Reason: " + escalation.getReason())
                    .setFontSize(11)
                    .setMarginBottom(3)
                    .setMarginLeft(10));
                if (escalation.getFollowUpStatus() != null) {
                    document.add(new Paragraph("Follow-up Status: " + escalation.getFollowUpStatus())
                        .setFontSize(11)
                        .setMarginBottom(5)
                        .setMarginLeft(10));
                }
                document.add(new Paragraph("\n"));
            }
        }

        // Meetings
        if (report.getMeetings() != null && !report.getMeetings().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("Meetings (Team Collaboration and External)")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            Table meetingTable = new Table(6).useAllAvailableWidth();
            meetingTable.setMarginBottom(10);
            meetingTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

            addStyledTableHeader(meetingTable, "Type");
            addStyledTableHeader(meetingTable, "Topic");
            addStyledTableHeader(meetingTable, "Summary");
            addStyledTableHeader(meetingTable, "Action Taken");
            addStyledTableHeader(meetingTable, "Next Step");
            addStyledTableHeader(meetingTable, "Participants");

            for (var meeting : report.getMeetings()) {
                addStyledTableCell(meetingTable, meeting.getMeetingType());
                addStyledTableCell(meetingTable, meeting.getTopic());
                addStyledTableCell(meetingTable, meeting.getSummary());
                addStyledTableCell(meetingTable, meeting.getActionTaken() != null ? meeting.getActionTaken() : "");
                addStyledTableCell(meetingTable, meeting.getNextStep() != null ? meeting.getNextStep() : "");
                addStyledTableCell(meetingTable, meeting.getParticipants() != null ? meeting.getParticipants() : "");
            }

            document.add(meetingTable);
            document.add(new Paragraph("\n"));
        }

        // AFPay Card Requests
        if (report.getAfpayCardRequests() != null && !report.getAfpayCardRequests().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("AFPay Card")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            Table afpayTable = new Table(6).useAllAvailableWidth();
            afpayTable.setMarginBottom(10);
            afpayTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

            addStyledTableHeader(afpayTable, "Type");
            addStyledTableHeader(afpayTable, "Requested By");
            addStyledTableHeader(afpayTable, "Request date");
            addStyledTableHeader(afpayTable, "Resolution");
            addStyledTableHeader(afpayTable, "Support Doc Scanned");
            addStyledTableHeader(afpayTable, "Date Archived");

            for (var afpay : report.getAfpayCardRequests()) {
                addStyledTableCell(afpayTable, afpay.getRequestType());
                addStyledTableCell(afpayTable, afpay.getRequestedBy());
                addStyledTableCell(afpayTable, afpay.getRequestDate() != null ? afpay.getRequestDate().toString() : "");
                addStyledTableCell(afpayTable, afpay.getResolutionDetails() != null ? afpay.getResolutionDetails() : "");
                addStyledTableCell(afpayTable, afpay.getSupportingDocumentPath() != null ? afpay.getSupportingDocumentPath() : "");
                addStyledTableCell(afpayTable, afpay.getArchivedDate() != null ? afpay.getArchivedDate().toString() : "");
            }

            document.add(afpayTable);
            document.add(new Paragraph("\n"));
        }

        // QRMIS Issues
        if (report.getQrmisIssues() != null && !report.getQrmisIssues().isEmpty()) {
            Paragraph sectionTitle = new Paragraph("QRMIS Issues / Tickets")
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setFontColor(new DeviceRgb(101, 67, 33)) // Dark brown
                .setBackgroundColor(new DeviceRgb(255, 228, 181)) // Moccasin
                .setPadding(10)
                .setBorderBottom(new SolidBorder(new DeviceRgb(139, 69, 19), 3));
            document.add(sectionTitle);

            Table qrmisTable = new Table(7).useAllAvailableWidth();
            qrmisTable.setMarginBottom(10);
            qrmisTable.setBorder(new SolidBorder(new DeviceRgb(139, 69, 19), 2));

            addStyledTableHeader(qrmisTable, "Problem Type");
            addStyledTableHeader(qrmisTable, "Problem Description");
            addStyledTableHeader(qrmisTable, "Solution provided");
            addStyledTableHeader(qrmisTable, "Posted by");
            addStyledTableHeader(qrmisTable, "Authorized By");
            addStyledTableHeader(qrmisTable, "Support Document");
            addStyledTableHeader(qrmisTable, "Operator");

            for (var qrmis : report.getQrmisIssues()) {
                addStyledTableCell(qrmisTable, qrmis.getProblemType());
                addStyledTableCell(qrmisTable, qrmis.getProblemDescription());
                addStyledTableCell(qrmisTable, qrmis.getSolutionProvided() != null ? qrmis.getSolutionProvided() : "");
                addStyledTableCell(qrmisTable, qrmis.getPostedBy() != null ? qrmis.getPostedBy() : "");
                addStyledTableCell(qrmisTable, qrmis.getAuthorizedBy() != null ? qrmis.getAuthorizedBy() : "");
                addStyledTableCell(qrmisTable, qrmis.getSupportingDocumentsArchived() != null ? qrmis.getSupportingDocumentsArchived() : "");
                addStyledTableCell(qrmisTable, qrmis.getOperator() != null ? qrmis.getOperator() : "");
            }

            document.add(qrmisTable);
        }
    }

    private void addTableHeader(Table table, String text) {
        addStyledTableHeader(table, text);
    }

    private void addTableCell(Table table, String text) {
        addStyledTableCell(table, text);
    }
}


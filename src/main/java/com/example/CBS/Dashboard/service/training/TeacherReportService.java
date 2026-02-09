package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.*;
import com.example.CBS.Dashboard.entity.Attendance;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.entity.Enrollment;
import com.example.CBS.Dashboard.entity.TrainingSession;
import com.example.CBS.Dashboard.repository.AttendanceRepository;
import com.example.CBS.Dashboard.repository.EnrollmentRepository;
import com.example.CBS.Dashboard.repository.TrainingSessionRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherReportService {

    private final TrainingSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public List<SessionAttendanceReportDto> getTeacherAttendanceReport(String username, LocalDate from, LocalDate to) {
        Long instructorId = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                .getId();

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<TrainingSession> sessions = sessionRepository.findByInstructorIdAndDateRange(instructorId, fromDateTime, toDateTime);

        List<SessionAttendanceReportDto> reportRows = new ArrayList<>();
        for (TrainingSession session : sessions) {
            LocalDateTime sessionStart = session.getStartDateTime();
            LocalDateTime startOfDay = sessionStart.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = sessionStart.toLocalDate().plusDays(1).atStartOfDay();

            List<Attendance> attendanceList = attendanceRepository.findBySessionIdAndAttendanceDateBetween(
                    session.getId(), startOfDay, endOfDay);

        List<Attendance> attendedList = attendanceList.stream()
                .filter(a ->
                        a.getStatus() == Attendance.AttendanceStatus.PRESENT
                                || a.getStatus() == Attendance.AttendanceStatus.LATE
                                || a.getStatus() == Attendance.AttendanceStatus.EXCUSED
                                || (a.getStatus() == Attendance.AttendanceStatus.ABSENT
                                    && a.getSignatureType() == Attendance.SignatureType.ACKNOWLEDGMENT))
                .collect(Collectors.toList());

            List<String> attendedNames = attendedList.stream()
                    .map(a -> a.getParticipant() != null ? a.getParticipant().getFullName() : null)
                    .filter(name -> name != null && !name.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            List<AttendeeSignatureDto> signatures = new ArrayList<>();
            try {
                java.util.Set<Long> seenParticipants = new java.util.HashSet<>();
                for (Attendance a : attendedList) {
                    if (a.getParticipant() == null) continue;
                    Long pid = a.getParticipant().getId();
                    if (seenParticipants.contains(pid)) continue;
                    seenParticipants.add(pid);
                    Hibernate.initialize(a.getParticipant());

                    // Prefer the recorded attendance signature for this session; fall back to profile signature.
                    String sigData = a.getSignatureData();
                    if (sigData == null || sigData.isBlank()) {
                        User u = userRepository.findById(pid).orElse(null);
                        if (u != null && u.getSignatureData() != null && !u.getSignatureData().isBlank()) {
                            sigData = u.getSignatureData();
                        }
                    }

                    signatures.add(new AttendeeSignatureDto(
                            pid,
                            a.getParticipant().getFullName(),
                            sigData));
                }
            } catch (Exception ex) {
                // Fallback: return report without signatures if signature fetch fails
            }

            String programTitle = session.getProgram() != null ? session.getProgram().getTitle() : "—";
            String topic = (session.getTopic() != null && !session.getTopic().isBlank()) ? session.getTopic() : "—";
            String instructorName = session.getInstructor() != null ? session.getInstructor().getFullName() : null;
            if (instructorName == null && session.getProgram() != null && session.getProgram().getInstructor() != null) {
                instructorName = session.getProgram().getInstructor().getFullName();
            }
            String sessionType = (session.getSessionType() != null && !session.getSessionType().isBlank())
                    ? session.getSessionType() : "—";
            String notes = (session.getNotes() != null && !session.getNotes().isBlank()) ? session.getNotes() : null;

            reportRows.add(new SessionAttendanceReportDto(
                    session.getId(),
                    session.getProgram() != null ? session.getProgram().getId() : null,
                    programTitle,
                    topic,
                    session.getStartDateTime(),
                    attendedNames,
                    signatures,
                    instructorName,
                    sessionType,
                    notes));
        }
        return reportRows;
    }

    @Transactional(readOnly = true)
    public SingleSessionReportDto getSingleSessionReport(String username, Long sessionId) {
        Long instructorId = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                .getId();

        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        Hibernate.initialize(session.getProgram());
        Hibernate.initialize(session.getInstructor());

        boolean hasAccess = (session.getProgram() != null && session.getProgram().getInstructor() != null
                && session.getProgram().getInstructor().getId().equals(instructorId))
                || (session.getInstructor() != null && session.getInstructor().getId().equals(instructorId));
        if (!hasAccess) {
            throw new RuntimeException("Access denied: you are not the instructor for this session");
        }

        Long programId = session.getProgram().getId();
        List<Enrollment> enrollments = enrollmentRepository.findByProgramId(programId);
        LocalDateTime startOfDay = session.getStartDateTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = session.getStartDateTime().toLocalDate().plusDays(1).atStartOfDay();
        List<Attendance> sessionAttendance = attendanceRepository.findBySessionIdAndAttendanceDateBetween(
                sessionId, startOfDay, endOfDay);
        Map<Long, Attendance> attendanceByParticipant = sessionAttendance.stream()
                .collect(Collectors.toMap(a -> a.getParticipant().getId(), a -> a, (a1, a2) -> a1));

        List<Attendance> allForSession = attendanceRepository.findBySessionId(sessionId);
        Map<Long, long[]> statsByParticipant = new HashMap<>();
        for (Attendance a : allForSession) {
            Long pid = a.getParticipant().getId();
            statsByParticipant.putIfAbsent(pid, new long[]{0L, 0L});
            long[] p = statsByParticipant.get(pid);
            if (a.getStatus() == Attendance.AttendanceStatus.PRESENT || a.getStatus() == Attendance.AttendanceStatus.LATE
                    || a.getStatus() == Attendance.AttendanceStatus.EXCUSED) {
                p[0]++;
            } else {
                p[1]++;
            }
        }

        int presentCount = 0, absentCount = 0, lateCount = 0, excusedCount = 0;
        List<StudentEngagementDto> engagement = new ArrayList<>();
        List<AttendeeSignatureDto> attendedSignatures = new ArrayList<>();
        for (Enrollment e : enrollments) {
            Hibernate.initialize(e.getParticipant());
            Long pid = e.getParticipant().getId();
            Attendance att = attendanceByParticipant.get(pid);
            String status = att != null ? att.getStatus().name() : "ABSENT";
            String notes = att != null ? att.getNotes() : null;

            if ("PRESENT".equals(status)) presentCount++;
            else if ("ABSENT".equals(status)) absentCount++;
            else if ("LATE".equals(status)) lateCount++;
            else if ("EXCUSED".equals(status)) excusedCount++;

            long[] stats = statsByParticipant.getOrDefault(pid, new long[]{0L, 0L});
            long total = stats[0] + stats[1];
            double pct = total > 0 ? (100.0 * stats[0] / total) : 100.0;

            String sigData = null;
            String sigType = null;
            if (att != null && att.getSignatureData() != null && !att.getSignatureData().isBlank()) {
                sigData = att.getSignatureData();
                sigType = att.getSignatureType() != null ? att.getSignatureType().name() : null;
            } else {
                // Fallback to profile signature for legacy records
                try {
                    User u = userRepository.findById(pid).orElse(null);
                    if (u != null && u.getSignatureData() != null && !u.getSignatureData().isBlank()) {
                        sigData = u.getSignatureData();
                    }
                } catch (Exception ignored) { }
            }

            boolean isSignedParticipant =
                    "PRESENT".equals(status) || "LATE".equals(status) || "EXCUSED".equals(status)
                            || ("ABSENT".equals(status) && "ACKNOWLEDGMENT".equals(sigType));
            if (isSignedParticipant && sigData != null) {
                attendedSignatures.add(new AttendeeSignatureDto(pid, e.getParticipant().getFullName(), sigData));
            }

            engagement.add(new StudentEngagementDto(
                    pid,
                    e.getParticipant().getFullName(),
                    e.getParticipant().getEmail(),
                    status,
                    notes,
                    Math.round(pct * 10) / 10.0,
                    sigData,
                    sigType));
        }

        List<String> contentCoverage = new ArrayList<>();
        String topic = (session.getTopic() != null && !session.getTopic().isBlank()) ? session.getTopic() : "—";
        contentCoverage.addAll(Arrays.stream(topic.split("[,\n]")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        if (contentCoverage.isEmpty()) contentCoverage.add(topic);

        String instructorName = session.getInstructor() != null ? session.getInstructor().getFullName() : null;
        if (instructorName == null && session.getProgram() != null && session.getProgram().getInstructor() != null) {
            instructorName = session.getProgram().getInstructor().getFullName();
        }
        String sessionType = (session.getSessionType() != null && !session.getSessionType().isBlank()) ? session.getSessionType() : "—";
        String notesVal = (session.getNotes() != null && !session.getNotes().isBlank()) ? session.getNotes() : null;

        return new SingleSessionReportDto(
                session.getId(),
                session.getProgram() != null ? session.getProgram().getTitle() : "—",
                topic,
                session.getStartDateTime(),
                instructorName,
                sessionType,
                notesVal,
                contentCoverage,
                engagement,
                attendedSignatures,
                presentCount,
                absentCount,
                lateCount,
                excusedCount,
                enrollments.size());
    }

    @Transactional(readOnly = true)
    public DateBasedGroupedReportDto getDateBasedGroupedReport(String username, LocalDate from, LocalDate to) {
        List<SessionAttendanceReportDto> sessions = getTeacherAttendanceReport(username, from, to);

        Set<Long> allStudentIds = new HashSet<>();
        Map<Long, Set<Long>> studentToAttendedSessions = new HashMap<>();
        Map<Long, List<String>> studentToTopics = new HashMap<>();
        Map<Long, String> studentNames = new HashMap<>();
        Map<Long, String> studentEmails = new HashMap<>();
        Map<Long, Set<Long>> studentToRelevantSessions = new HashMap<>();

        for (SessionAttendanceReportDto s : sessions) {
            Long programId = s.getProgramId();
            if (programId == null) continue;
            List<Enrollment> enrollments = enrollmentRepository.findByProgramId(programId);
            Set<String> attendedNames = s.getAttendedStudentNames() != null
                    ? new HashSet<>(s.getAttendedStudentNames()) : new HashSet<>();

            for (Enrollment e : enrollments) {
                if (e.getParticipant() == null) continue;
                Hibernate.initialize(e.getParticipant());
                Long pid = e.getParticipant().getId();
                allStudentIds.add(pid);
                studentNames.putIfAbsent(pid, e.getParticipant().getFullName());
                studentEmails.putIfAbsent(pid, e.getParticipant().getEmail());
                studentToRelevantSessions.computeIfAbsent(pid, k -> new HashSet<>()).add(s.getSessionId());

                if (attendedNames.contains(e.getParticipant().getFullName())) {
                    studentToAttendedSessions.computeIfAbsent(pid, k -> new HashSet<>()).add(s.getSessionId());
                    studentToTopics.computeIfAbsent(pid, k -> new ArrayList<>()).add(s.getSessionTopic());
                }
            }
        }

        List<StudentParticipationDto> byStudent = new ArrayList<>();
        int totalSessions = sessions.size();
        double sumPct = 0;
        int studentCount = 0;

        for (Long pid : allStudentIds.stream().sorted().collect(Collectors.toList())) {
            Set<Long> relevantSessions = studentToRelevantSessions.getOrDefault(pid, new HashSet<>());
            Set<Long> attended = studentToAttendedSessions.getOrDefault(pid, new HashSet<>());
            int relevantCount = relevantSessions.size();
            int attendedCount = attended.size();
            double pct = relevantCount > 0 ? (100.0 * attendedCount / relevantCount) : 100.0;
            sumPct += pct;
            studentCount++;

            String trend = pct >= 80 ? "Good" : (pct >= 60 ? "Moderate" : "Needs Attention");
            byStudent.add(new StudentParticipationDto(
                    pid,
                    studentNames.getOrDefault(pid, "—"),
                    studentEmails.getOrDefault(pid, ""),
                    attendedCount,
                    relevantCount,
                    Math.round(pct * 10) / 10.0,
                    studentToTopics.getOrDefault(pid, new ArrayList<>()),
                    trend));
        }

        double overallRate = studentCount > 0 ? sumPct / studentCount : 0;

        return new DateBasedGroupedReportDto(
                from,
                to,
                sessions,
                byStudent,
                totalSessions,
                allStudentIds.size(),
                Math.round(overallRate * 10) / 10.0);
    }
}

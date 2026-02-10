package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.CfoTrainingDashboardDto;
import com.example.CBS.Dashboard.entity.Attendance;
import com.example.CBS.Dashboard.entity.Enrollment;
import com.example.CBS.Dashboard.entity.TrainingProgram;
import com.example.CBS.Dashboard.entity.TrainingSession;
import com.example.CBS.Dashboard.repository.AttendanceRepository;
import com.example.CBS.Dashboard.repository.EnrollmentRepository;
import com.example.CBS.Dashboard.repository.TrainingProgramRepository;
import com.example.CBS.Dashboard.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregated analytics for the CFO Training Oversight dashboard.
 *
 * This service is read-only and computes portfolio-wide metrics over a date window.
 */
@Service
@RequiredArgsConstructor
public class CfoTrainingAnalyticsService {

    private final TrainingProgramRepository programRepository;
    private final TrainingSessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Build the CFO dashboard for the given date range.
     * If dates are null, the last 3 months (90 days) are used by default.
     */
    @Transactional(readOnly = true)
    public CfoTrainingDashboardDto getDashboard(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        if (from == null || to == null) {
            to = today;
            from = today.minusDays(90);
        }
        if (to.isBefore(from)) {
            // Swap if caller sends inverted dates
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<TrainingProgram> allPrograms = programRepository.findAllByOrderByCreatedAtDesc();
        List<TrainingSession> allSessions = sessionRepository.findAll();
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        List<Attendance> allAttendance = attendanceRepository.findAll();

        // Filter sessions to the requested date window
        List<TrainingSession> sessionsInRange = allSessions.stream()
                .filter(s -> s.getStartDateTime() != null
                        && !s.getStartDateTime().isBefore(fromDateTime)
                        && s.getStartDateTime().isBefore(toDateTime))
                .collect(Collectors.toList());

        // Programs that have at least one session in the window
        Set<Long> programIdsInRange = sessionsInRange.stream()
                .map(TrainingSession::getProgram)
                .filter(Objects::nonNull)
                .map(TrainingProgram::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<TrainingProgram> programsInRange = allPrograms.stream()
                .filter(p -> p.getId() != null && programIdsInRange.contains(p.getId()))
                .collect(Collectors.toList());

        // Group sessions per program
        Map<Long, List<TrainingSession>> sessionsByProgram = new HashMap<>();
        for (TrainingSession session : sessionsInRange) {
            TrainingProgram program = session.getProgram();
            if (program == null || program.getId() == null) continue;
            sessionsByProgram
                    .computeIfAbsent(program.getId(), id -> new ArrayList<>())
                    .add(session);
        }

        // Enrollments per program (only for programs that appear in the window)
        Map<Long, List<Enrollment>> enrollmentsByProgram = new HashMap<>();
        for (Enrollment enrollment : allEnrollments) {
            TrainingProgram program = enrollment.getProgram();
            if (program == null || program.getId() == null) continue;
            if (!programIdsInRange.contains(program.getId())) continue;
            enrollmentsByProgram
                    .computeIfAbsent(program.getId(), id -> new ArrayList<>())
                    .add(enrollment);
        }

        // Attendance grouped by session for sessions in range
        Map<Long, List<Attendance>> attendanceBySession = new HashMap<>();
        for (Attendance attendance : allAttendance) {
            TrainingSession session = attendance.getSession();
            if (session == null || session.getId() == null || session.getStartDateTime() == null) {
                continue;
            }
            if (session.getStartDateTime().isBefore(fromDateTime) || !session.getStartDateTime().isBefore(toDateTime)) {
                continue;
            }
            attendanceBySession
                    .computeIfAbsent(session.getId(), id -> new ArrayList<>())
                    .add(attendance);
        }

        // ---- Summary KPIs ----
        CfoTrainingDashboardDto.SummaryKpis summary = buildSummaryKpis(
                programsInRange,
                sessionsInRange,
                enrollmentsByProgram,
                attendanceBySession
        );

        // ---- Program performance rows ----
        List<CfoTrainingDashboardDto.ProgramPerformanceRow> programPerformance =
                buildProgramPerformance(programsInRange, sessionsByProgram, enrollmentsByProgram, attendanceBySession);

        // ---- Monthly / quarterly trend ----
        List<CfoTrainingDashboardDto.TimeSeriesPoint> performanceTrend =
                buildPerformanceTrend(sessionsInRange, attendanceBySession);

        // ---- Department-level attendance ----
        List<CfoTrainingDashboardDto.CategoryValue> departmentAttendance =
                buildDepartmentAttendance(attendanceBySession);

        // ---- Instructor productivity ----
        List<CfoTrainingDashboardDto.InstructorMetric> instructorMetrics =
                buildInstructorMetrics(sessionsInRange, attendanceBySession);

        // ---- Material usage (synthetic index based on program flags) ----
        List<CfoTrainingDashboardDto.CategoryValue> materialUsage =
                buildMaterialUsage(programsInRange);

        // ---- Process efficiency ----
        CfoTrainingDashboardDto.ProcessEfficiency processEfficiency =
                buildProcessEfficiency(programsInRange, sessionsByProgram);

        // ---- Risk panel ----
        List<CfoTrainingDashboardDto.RiskItem> risks =
                buildRisks(programPerformance, departmentAttendance);

        // Populate underperforming program count on the summary from risk list.
        long underperformingCount = risks.stream()
                .filter(r -> "PROGRAM_UNDERPERFORMING".equalsIgnoreCase(r.getType()))
                .count();
        summary.setUnderperformingPrograms(underperformingCount);

        CfoTrainingDashboardDto dto = new CfoTrainingDashboardDto();
        dto.setSummary(summary);
        dto.setProgramPerformance(programPerformance);
        dto.setPerformanceTrend(performanceTrend);
        dto.setDepartmentAttendance(departmentAttendance);
        dto.setInstructorProductivity(instructorMetrics);
        dto.setMaterialUsage(materialUsage);
        dto.setProcessEfficiency(processEfficiency);
        dto.setRisks(risks);
        return dto;
    }

    // -------------------------------------------------------------------------
    // Helpers – Summary
    // -------------------------------------------------------------------------

    private CfoTrainingDashboardDto.SummaryKpis buildSummaryKpis(
            List<TrainingProgram> programs,
            List<TrainingSession> sessions,
            Map<Long, List<Enrollment>> enrollmentsByProgram,
            Map<Long, List<Attendance>> attendanceBySession
    ) {
        long totalPrograms = programs.size();
        long activePrograms = programs.stream()
                .filter(p -> p.getStatus() == TrainingProgram.TrainingStatus.ONGOING
                        || p.getStatus() == TrainingProgram.TrainingStatus.PUBLISHED)
                .count();
        long completedPrograms = programs.stream()
                .filter(p -> p.getStatus() == TrainingProgram.TrainingStatus.COMPLETED)
                .count();

        long totalSessions = sessions.size();
        long completedSessions = sessions.stream()
                .filter(s -> s.getStatus() == TrainingSession.SessionStatus.COMPLETED)
                .count();

        double sessionCompletionRate = totalSessions > 0
                ? roundPercent(100.0 * completedSessions / totalSessions)
                : 0.0;

        int attendanceTotal = 0;
        int attendancePresentLike = 0;
        for (List<Attendance> list : attendanceBySession.values()) {
            for (Attendance a : list) {
                attendanceTotal++;
                if (isPresentLike(a)) {
                    attendancePresentLike++;
                }
            }
        }
        double studentAttendanceRate = attendanceTotal > 0
                ? roundPercent(100.0 * attendancePresentLike / attendanceTotal)
                : 0.0;

        long sessionsWithInstructor = sessions.stream()
                .filter(this::hasInstructorAssigned)
                .count();
        double teacherParticipationRate = totalSessions > 0
                ? roundPercent(100.0 * sessionsWithInstructor / totalSessions)
                : 0.0;

        // Simple material engagement proxy – programs that have any material flag set.
        long programsWithMaterials = programs.stream()
                .filter(p -> Boolean.TRUE.equals(p.getHasArticleMaterial())
                        || Boolean.TRUE.equals(p.getHasVideoMaterial())
                        || Boolean.TRUE.equals(p.getHasSlideMaterial()))
                .count();
        double materialEngagementScore = totalPrograms > 0
                ? roundPercent(100.0 * programsWithMaterials / totalPrograms)
                : 0.0;

        // Utilization: confirmed+in-progress enrollments vs total capacity from sessions.
        long totalCapacity = sessions.stream()
                .filter(s -> s.getMaxCapacity() != null && s.getMaxCapacity() > 0)
                .mapToLong(s -> s.getMaxCapacity())
                .sum();

        long activeEnrollments = enrollmentsByProgram.values().stream()
                .flatMap(Collection::stream)
                .filter(e -> e.getStatus() != Enrollment.EnrollmentStatus.CANCELLED
                        && e.getStatus() != Enrollment.EnrollmentStatus.WITHDRAWN)
                .count();

        double utilizationRate = totalCapacity > 0
                ? roundPercent(100.0 * activeEnrollments / totalCapacity)
                : 0.0;

        // Average completion time (days) from program training date to last completed session end.
        Double avgCompletionDays = computeAverageCompletionDays(programs, sessions);

        // Underperforming programs and delayed sessions will be populated in risk builder,
        // but we expose high-level counts here.
        long delayedOrCancelledSessions = sessions.stream()
                .filter(s -> s.getStatus() == TrainingSession.SessionStatus.CANCELLED
                        || s.getStatus() == TrainingSession.SessionStatus.POSTPONED)
                .count();

        CfoTrainingDashboardDto.SummaryKpis summary = new CfoTrainingDashboardDto.SummaryKpis();
        summary.setTotalPrograms(totalPrograms);
        summary.setActivePrograms(activePrograms);
        summary.setCompletedPrograms(completedPrograms);
        summary.setTotalSessions(totalSessions);
        summary.setSessionCompletionRate(sessionCompletionRate);
        summary.setStudentAttendanceRate(studentAttendanceRate);
        summary.setTeacherParticipationRate(teacherParticipationRate);
        summary.setMaterialEngagementScore(materialEngagementScore);
        summary.setTrainingUtilizationRate(utilizationRate);
        summary.setAverageCompletionTimeDays(avgCompletionDays);
        summary.setDelayedOrCancelledSessions(delayedOrCancelledSessions);

        // Financial indices can be wired once cost data exists.
        summary.setProgramCostPerformanceIndex(null);
        summary.setTrainingRoiIndicator(null);

        // Operational efficiency – basic proxy combining completion & attendance & utilization.
        double efficiencyScore = (sessionCompletionRate + studentAttendanceRate + utilizationRate) / 3.0;
        summary.setOperationalEfficiencyScore(roundPercent(efficiencyScore));

        // Underperforming count is set in risk builder to keep threshold logic in one place.
        summary.setUnderperformingPrograms(null);
        return summary;
    }

    private Double computeAverageCompletionDays(List<TrainingProgram> programs, List<TrainingSession> sessions) {
        if (programs.isEmpty() || sessions.isEmpty()) {
            return null;
        }
        Map<Long, List<TrainingSession>> completedByProgram = sessions.stream()
                .filter(s -> s.getStatus() == TrainingSession.SessionStatus.COMPLETED
                        && s.getProgram() != null
                        && s.getProgram().getId() != null)
                .collect(Collectors.groupingBy(s -> s.getProgram().getId()));

        long totalProgramsWithCompletion = 0;
        double sumDays = 0.0;

        for (TrainingProgram program : programs) {
            if (program.getId() == null || program.getTrainingDate() == null) continue;
            List<TrainingSession> programSessions = completedByProgram.get(program.getId());
            if (programSessions == null || programSessions.isEmpty()) continue;

            LocalDate programDate = program.getTrainingDate();
            LocalDate lastCompletionDate = programSessions.stream()
                    .map(TrainingSession::getEndDateTime)
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            if (lastCompletionDate == null) continue;

            long days = Duration.between(programDate.atStartOfDay(), lastCompletionDate.atStartOfDay()).toDays();
            if (days < 0) {
                // If sessions are scheduled before the declared training date, treat as 0 lag.
                days = 0;
            }
            sumDays += days;
            totalProgramsWithCompletion++;
        }

        if (totalProgramsWithCompletion == 0) {
            return null;
        }
        double avg = sumDays / totalProgramsWithCompletion;
        return Math.round(avg * 10.0) / 10.0;
    }

    // -------------------------------------------------------------------------
    // Helpers – Program performance
    // -------------------------------------------------------------------------

    private List<CfoTrainingDashboardDto.ProgramPerformanceRow> buildProgramPerformance(
            List<TrainingProgram> programs,
            Map<Long, List<TrainingSession>> sessionsByProgram,
            Map<Long, List<Enrollment>> enrollmentsByProgram,
            Map<Long, List<Attendance>> attendanceBySession
    ) {
        List<CfoTrainingDashboardDto.ProgramPerformanceRow> rows = new ArrayList<>();

        for (TrainingProgram program : programs) {
            Long programId = program.getId();
            if (programId == null) continue;

            List<TrainingSession> programSessions = sessionsByProgram.getOrDefault(programId, Collections.emptyList());
            List<Enrollment> programEnrollments = enrollmentsByProgram.getOrDefault(programId, Collections.emptyList());

            long sessionsPlanned = programSessions.size();
            long sessionsCompleted = programSessions.stream()
                    .filter(s -> s.getStatus() == TrainingSession.SessionStatus.COMPLETED)
                    .count();

            long enrollmentsCount = programEnrollments.size();

            int programAttendanceTotal = 0;
            int programAttendancePresentLike = 0;
            long totalDurationMinutes = 0;
            int durationSessionsCount = 0;

            for (TrainingSession session : programSessions) {
                List<Attendance> sessionAttendance = attendanceBySession.getOrDefault(session.getId(), Collections.emptyList());
                for (Attendance a : sessionAttendance) {
                    programAttendanceTotal++;
                    if (isPresentLike(a)) {
                        programAttendancePresentLike++;
                    }
                }

                if (session.getStartDateTime() != null && session.getEndDateTime() != null) {
                    long minutes = Duration.between(session.getStartDateTime(), session.getEndDateTime()).toMinutes();
                    if (minutes > 0) {
                        totalDurationMinutes += minutes;
                        durationSessionsCount++;
                    }
                }
            }

            double completionRate = sessionsPlanned > 0
                    ? roundPercent(100.0 * sessionsCompleted / sessionsPlanned)
                    : 0.0;

            double attendanceRate = programAttendanceTotal > 0
                    ? roundPercent(100.0 * programAttendancePresentLike / programAttendanceTotal)
                    : 0.0;

            Double avgDurationHours = null;
            if (durationSessionsCount > 0) {
                double hours = (totalDurationMinutes / (double) durationSessionsCount) / 60.0;
                avgDurationHours = Math.round(hours * 10.0) / 10.0;
            }

            String departmentName = null;
            if (program.getDepartment() != null) {
                departmentName = program.getDepartment().getName();
            }

            CfoTrainingDashboardDto.ProgramPerformanceRow row = new CfoTrainingDashboardDto.ProgramPerformanceRow();
            row.setProgramId(programId);
            row.setProgramTitle(program.getTitle());
            row.setDepartmentName(departmentName);
            row.setStatus(program.getStatus() != null ? program.getStatus().name() : null);
            row.setSessionsPlanned(sessionsPlanned);
            row.setSessionsCompleted(sessionsCompleted);
            row.setTotalEnrollments(enrollmentsCount);
            row.setCompletionRate(completionRate);
            row.setAttendanceRate(attendanceRate);
            row.setAverageSessionDurationHours(avgDurationHours);
            rows.add(row);
        }

        // Sort by underperformance first (lowest completion/attendance first), then by most recent created
        rows.sort(Comparator
                .comparingDouble(CfoTrainingDashboardDto.ProgramPerformanceRow::getCompletionRate)
                .thenComparingDouble(CfoTrainingDashboardDto.ProgramPerformanceRow::getAttendanceRate));

        return rows;
    }

    // -------------------------------------------------------------------------
    // Helpers – Time series trend
    // -------------------------------------------------------------------------

    private List<CfoTrainingDashboardDto.TimeSeriesPoint> buildPerformanceTrend(
            List<TrainingSession> sessions,
            Map<Long, List<Attendance>> attendanceBySession
    ) {
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<YearMonth, long[]> monthStats = new HashMap<>();
        for (TrainingSession session : sessions) {
            if (session.getStartDateTime() == null) continue;
            YearMonth ym = YearMonth.from(session.getStartDateTime());
            long[] stats = monthStats.computeIfAbsent(ym, k -> new long[]{0L, 0L, 0L, 0L});
            // index 0: present-like attendance
            // index 1: total attendance
            // index 2: completed sessions
            // index 3: total sessions
            stats[3]++;
            if (session.getStatus() == TrainingSession.SessionStatus.COMPLETED) {
                stats[2]++;
            }
            List<Attendance> attendanceForSession = attendanceBySession.getOrDefault(session.getId(), Collections.emptyList());
            for (Attendance a : attendanceForSession) {
                stats[1]++;
                if (isPresentLike(a)) {
                    stats[0]++;
                }
            }
        }

        List<YearMonth> months = new ArrayList<>(monthStats.keySet());
        Collections.sort(months);

        List<CfoTrainingDashboardDto.TimeSeriesPoint> points = new ArrayList<>();
        for (YearMonth ym : months) {
            long[] stats = monthStats.get(ym);
            long presentLike = stats[0];
            long attendanceTotal = stats[1];
            long completedSessions = stats[2];
            long totalSessions = stats[3];

            double attendanceRate = attendanceTotal > 0
                    ? roundPercent(100.0 * presentLike / attendanceTotal)
                    : 0.0;
            double completionRate = totalSessions > 0
                    ? roundPercent(100.0 * completedSessions / totalSessions)
                    : 0.0;

            CfoTrainingDashboardDto.TimeSeriesPoint point = new CfoTrainingDashboardDto.TimeSeriesPoint();
            point.setPeriod(ym.toString()); // e.g. 2025-01
            point.setAttendanceRate(attendanceRate);
            point.setCompletionRate(completionRate);
            point.setEngagementScore(attendanceRate);
            point.setCostEfficiencyIndex(null); // will be populated once cost data is wired
            points.add(point);
        }
        return points;
    }

    // -------------------------------------------------------------------------
    // Helpers – Department attendance
    // -------------------------------------------------------------------------

    private List<CfoTrainingDashboardDto.CategoryValue> buildDepartmentAttendance(
            Map<Long, List<Attendance>> attendanceBySession
    ) {
        if (attendanceBySession.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, long[]> deptStats = new HashMap<>();

        for (Map.Entry<Long, List<Attendance>> entry : attendanceBySession.entrySet()) {
            for (Attendance a : entry.getValue()) {
                TrainingSession session = a.getSession();
                if (session == null || session.getProgram() == null) continue;
                TrainingProgram program = session.getProgram();
                String deptName = (program.getDepartment() != null && program.getDepartment().getName() != null)
                        ? program.getDepartment().getName()
                        : "Unassigned";

                long[] stats = deptStats.computeIfAbsent(deptName, k -> new long[]{0L, 0L});
                // 0: present-like, 1: total
                stats[1]++;
                if (isPresentLike(a)) {
                    stats[0]++;
                }
            }
        }

        List<CfoTrainingDashboardDto.CategoryValue> result = new ArrayList<>();
        for (Map.Entry<String, long[]> e : deptStats.entrySet()) {
            String dept = e.getKey();
            long[] stats = e.getValue();
            long presentLike = stats[0];
            long total = stats[1];
            double rate = total > 0 ? roundPercent(100.0 * presentLike / total) : 0.0;

            CfoTrainingDashboardDto.CategoryValue cv = new CfoTrainingDashboardDto.CategoryValue();
            cv.setCategory(dept);
            cv.setValue(rate);
            cv.setSecondaryValue((double) total); // expose sample size as secondary
            result.add(cv);
        }

        // Sort by attendance descending
        result.sort(Comparator.comparingDouble(CfoTrainingDashboardDto.CategoryValue::getValue).reversed());
        return result;
    }

    // -------------------------------------------------------------------------
    // Helpers – Instructor metrics
    // -------------------------------------------------------------------------

    private List<CfoTrainingDashboardDto.InstructorMetric> buildInstructorMetrics(
            List<TrainingSession> sessions,
            Map<Long, List<Attendance>> attendanceBySession
    ) {
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        class Agg {
            String name;
            long sessionsTotal;
            long sessionsCompleted;
            long presentLike;
            long attendanceTotal;
        }

        Map<Long, Agg> byInstructor = new HashMap<>();

        for (TrainingSession session : sessions) {
            Long instructorId = null;
            String instructorName = null;
            if (session.getInstructor() != null && session.getInstructor().getId() != null) {
                instructorId = session.getInstructor().getId();
                instructorName = session.getInstructor().getFullName();
            } else if (session.getProgram() != null
                    && session.getProgram().getInstructor() != null
                    && session.getProgram().getInstructor().getId() != null) {
                instructorId = session.getProgram().getInstructor().getId();
                instructorName = session.getProgram().getInstructor().getFullName();
            }
            if (instructorId == null) continue;

            Agg agg = byInstructor.computeIfAbsent(instructorId, id -> new Agg());
            if (agg.name == null && instructorName != null) {
                agg.name = instructorName;
            }
            agg.sessionsTotal++;
            if (session.getStatus() == TrainingSession.SessionStatus.COMPLETED) {
                agg.sessionsCompleted++;
            }

            List<Attendance> attendance = attendanceBySession.getOrDefault(session.getId(), Collections.emptyList());
            for (Attendance a : attendance) {
                agg.attendanceTotal++;
                if (isPresentLike(a)) {
                    agg.presentLike++;
                }
            }
        }

        List<CfoTrainingDashboardDto.InstructorMetric> result = new ArrayList<>();
        for (Map.Entry<Long, Agg> entry : byInstructor.entrySet()) {
            Long id = entry.getKey();
            Agg agg = entry.getValue();
            if (agg.sessionsTotal == 0) continue;

            double reliability = roundPercent(100.0 * agg.sessionsCompleted / agg.sessionsTotal);
            double engagement = agg.attendanceTotal > 0
                    ? roundPercent(100.0 * agg.presentLike / agg.attendanceTotal)
                    : 0.0;

            CfoTrainingDashboardDto.InstructorMetric metric = new CfoTrainingDashboardDto.InstructorMetric();
            metric.setInstructorId(id);
            metric.setInstructorName(agg.name != null ? agg.name : "Instructor " + id);
            metric.setSessionsConducted(agg.sessionsTotal);
            metric.setReliabilityRate(reliability);
            metric.setEngagementScore(engagement);
            metric.setCoordinatorExecutionRate(null); // can be wired once coordinator metrics are available
            result.add(metric);
        }

        // Sort leaderboard by sessions conducted descending
        result.sort(Comparator.comparingLong(CfoTrainingDashboardDto.InstructorMetric::getSessionsConducted).reversed());
        return result;
    }

    // -------------------------------------------------------------------------
    // Helpers – Material usage
    // -------------------------------------------------------------------------

    private List<CfoTrainingDashboardDto.CategoryValue> buildMaterialUsage(List<TrainingProgram> programs) {
        if (programs.isEmpty()) {
            return Collections.emptyList();
        }

        long articleCount = programs.stream()
                .filter(p -> Boolean.TRUE.equals(p.getHasArticleMaterial()))
                .count();
        long videoCount = programs.stream()
                .filter(p -> Boolean.TRUE.equals(p.getHasVideoMaterial()))
                .count();
        long slideCount = programs.stream()
                .filter(p -> Boolean.TRUE.equals(p.getHasSlideMaterial()))
                .count();
        long totalPrograms = programs.size();

        List<CfoTrainingDashboardDto.CategoryValue> result = new ArrayList<>();

        CfoTrainingDashboardDto.CategoryValue articles = new CfoTrainingDashboardDto.CategoryValue();
        articles.setCategory("Articles");
        articles.setValue(totalPrograms > 0 ? roundPercent(100.0 * articleCount / totalPrograms) : 0.0);
        articles.setSecondaryValue((double) articleCount);
        result.add(articles);

        CfoTrainingDashboardDto.CategoryValue videos = new CfoTrainingDashboardDto.CategoryValue();
        videos.setCategory("Videos");
        videos.setValue(totalPrograms > 0 ? roundPercent(100.0 * videoCount / totalPrograms) : 0.0);
        videos.setSecondaryValue((double) videoCount);
        result.add(videos);

        CfoTrainingDashboardDto.CategoryValue slides = new CfoTrainingDashboardDto.CategoryValue();
        slides.setCategory("Slides");
        slides.setValue(totalPrograms > 0 ? roundPercent(100.0 * slideCount / totalPrograms) : 0.0);
        slides.setSecondaryValue((double) slideCount);
        result.add(slides);

        return result;
    }

    // -------------------------------------------------------------------------
    // Helpers – Process efficiency
    // -------------------------------------------------------------------------

    private CfoTrainingDashboardDto.ProcessEfficiency buildProcessEfficiency(
            List<TrainingProgram> programs,
            Map<Long, List<TrainingSession>> sessionsByProgram
    ) {
        if (programs.isEmpty()) {
            return new CfoTrainingDashboardDto.ProcessEfficiency(null, null, 0.0, 0.0, 0.0);
        }

        double sumFirstDays = 0.0;
        double sumCompletionDays = 0.0;
        long countFirst = 0;
        long countCompletion = 0;

        long totalSessions = 0;
        long delayedOrCancelled = 0;

        for (TrainingProgram program : programs) {
            Long programId = program.getId();
            if (programId == null) continue;
            List<TrainingSession> sessions = sessionsByProgram.getOrDefault(programId, Collections.emptyList());
            totalSessions += sessions.size();

            for (TrainingSession s : sessions) {
                if (s.getStatus() == TrainingSession.SessionStatus.CANCELLED
                        || s.getStatus() == TrainingSession.SessionStatus.POSTPONED) {
                    delayedOrCancelled++;
                }
            }

            if (program.getTrainingDate() == null || sessions.isEmpty()) continue;
            LocalDate programDate = program.getTrainingDate();

            LocalDate firstSessionDate = sessions.stream()
                    .map(TrainingSession::getStartDateTime)
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            if (firstSessionDate != null) {
                long days = Duration.between(programDate.atStartOfDay(), firstSessionDate.atStartOfDay()).toDays();
                if (days < 0) days = 0;
                sumFirstDays += days;
                countFirst++;
            }

            LocalDate lastCompletionDate = sessions.stream()
                    .filter(s -> s.getStatus() == TrainingSession.SessionStatus.COMPLETED)
                    .map(TrainingSession::getEndDateTime)
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            if (lastCompletionDate != null) {
                long days = Duration.between(programDate.atStartOfDay(), lastCompletionDate.atStartOfDay()).toDays();
                if (days < 0) days = 0;
                sumCompletionDays += days;
                countCompletion++;
            }
        }

        Double avgFirstDays = countFirst > 0 ? Math.round((sumFirstDays / countFirst) * 10.0) / 10.0 : null;
        Double avgCompletionDays = countCompletion > 0 ? Math.round((sumCompletionDays / countCompletion) * 10.0) / 10.0 : null;

        // Scheduling efficiency: shorter lead time to first session is better.
        double schedulingEfficiency = 0.0;
        if (avgFirstDays != null) {
            // Map 0 days -> 100, 30+ days -> ~10
            double penalty = Math.min(avgFirstDays, 30.0) * 3.0;
            schedulingEfficiency = roundPercent(Math.max(10.0, 100.0 - penalty));
        }

        // Completion timeline compliance: shorter completion windows are preferred.
        double completionCompliance = 0.0;
        if (avgCompletionDays != null) {
            // Map 0–60 days into 100–40 range roughly.
            double penalty = Math.min(avgCompletionDays, 60.0) * 1.0;
            completionCompliance = roundPercent(Math.max(40.0, 100.0 - penalty));
        }

        double processWasteIndex = 0.0;
        if (totalSessions > 0) {
            // Higher percentage of cancelled/postponed sessions means more waste.
            processWasteIndex = roundPercent(100.0 * delayedOrCancelled / totalSessions);
        }

        return new CfoTrainingDashboardDto.ProcessEfficiency(
                avgFirstDays,
                avgCompletionDays,
                schedulingEfficiency,
                completionCompliance,
                processWasteIndex
        );
    }

    // -------------------------------------------------------------------------
    // Helpers – Risk panel
    // -------------------------------------------------------------------------

    private List<CfoTrainingDashboardDto.RiskItem> buildRisks(
            List<CfoTrainingDashboardDto.ProgramPerformanceRow> programRows,
            List<CfoTrainingDashboardDto.CategoryValue> departmentAttendance
    ) {
        List<CfoTrainingDashboardDto.RiskItem> risks = new ArrayList<>();
        for (CfoTrainingDashboardDto.ProgramPerformanceRow row : programRows) {
            double completion = row.getCompletionRate();
            double attendance = row.getAttendanceRate();
            boolean underperforming = completion < 60.0 || attendance < 60.0;
            if (!underperforming) continue;
            String severity;
            if (completion < 40.0 || attendance < 40.0) {
                severity = "RED";
            } else {
                severity = "YELLOW";
            }

            String summary = String.format("Completion %.1f%%, Attendance %.1f%%", completion, attendance);
            CfoTrainingDashboardDto.RiskItem item = new CfoTrainingDashboardDto.RiskItem(
                    "PROGRAM_UNDERPERFORMING",
                    row.getProgramTitle(),
                    severity,
                    summary
            );
            risks.add(item);
        }

        for (CfoTrainingDashboardDto.CategoryValue dept : departmentAttendance) {
            double rate = dept.getValue();
            if (rate >= 70.0) continue;
            String severity = rate < 50.0 ? "RED" : "YELLOW";
            String summary = String.format("Department attendance at %.1f%%", rate);
            risks.add(new CfoTrainingDashboardDto.RiskItem(
                    "DEPARTMENT_LOW_ATTENDANCE",
                    dept.getCategory(),
                    severity,
                    summary
            ));
        }

        // Update summary-level count if we have any summary instance
        // (caller will merge this count back if needed).
        // We return the list; caller may also derive underperforming count from list.
        return risks;
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    private boolean isPresentLike(Attendance a) {
        if (a == null || a.getStatus() == null) return false;
        return a.getStatus() == Attendance.AttendanceStatus.PRESENT
                || a.getStatus() == Attendance.AttendanceStatus.LATE
                || a.getStatus() == Attendance.AttendanceStatus.EXCUSED;
    }

    private boolean hasInstructorAssigned(TrainingSession session) {
        if (session.getInstructor() != null && session.getInstructor().getId() != null) {
            return true;
        }
        return session.getProgram() != null
                && session.getProgram().getInstructor() != null
                && session.getProgram().getInstructor().getId() != null;
    }

    private double roundPercent(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}


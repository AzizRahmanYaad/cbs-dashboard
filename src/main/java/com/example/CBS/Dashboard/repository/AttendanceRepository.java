package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT DISTINCT a FROM Attendance a " +
           "LEFT JOIN FETCH a.session " +
           "LEFT JOIN FETCH a.participant " +
           "LEFT JOIN FETCH a.markedBy " +
           "WHERE a.session.id = :sessionId")
    List<Attendance> findBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT DISTINCT a FROM Attendance a " +
           "LEFT JOIN FETCH a.session " +
           "LEFT JOIN FETCH a.participant " +
           "LEFT JOIN FETCH a.markedBy " +
           "WHERE a.session.id = :sessionId AND a.participant.id = :participantId")
    Optional<Attendance> findBySessionIdAndParticipantId(@Param("sessionId") Long sessionId, 
                                                          @Param("participantId") Long participantId);
    
    @Query("SELECT DISTINCT a FROM Attendance a " +
           "LEFT JOIN FETCH a.session " +
           "LEFT JOIN FETCH a.participant " +
           "LEFT JOIN FETCH a.markedBy " +
           "WHERE a.participant.id = :participantId")
    List<Attendance> findByParticipantId(@Param("participantId") Long participantId);

    @Query("SELECT DISTINCT a FROM Attendance a " +
           "LEFT JOIN FETCH a.session " +
           "LEFT JOIN FETCH a.participant " +
           "LEFT JOIN FETCH a.markedBy " +
           "WHERE a.session.id = :sessionId AND a.attendanceDate >= :from AND a.attendanceDate < :to")
    List<Attendance> findBySessionIdAndAttendanceDateBetween(@Param("sessionId") Long sessionId,
                                                            @Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to);
}

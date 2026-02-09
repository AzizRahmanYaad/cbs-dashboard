package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.StudentTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentTeacherRepository extends JpaRepository<StudentTeacher, Long> {

    /** User IDs that already have a student_teachers record (student or teacher). */
    @Query("SELECT st.user.id FROM StudentTeacher st")
    List<Long> findAllAssignedUserIds();
    @Query("SELECT DISTINCT st FROM StudentTeacher st " +
           "LEFT JOIN FETCH st.user " +
           "LEFT JOIN FETCH st.createdBy " +
           "WHERE st.isActive = true AND st.type = :type " +
           "ORDER BY st.user.username ASC")
    List<StudentTeacher> findAllActiveByType(@Param("type") StudentTeacher.Type type);
    
    @Query("SELECT DISTINCT st FROM StudentTeacher st " +
           "LEFT JOIN FETCH st.user " +
           "LEFT JOIN FETCH st.createdBy " +
           "WHERE st.isActive = true " +
           "ORDER BY st.type ASC, st.user.username ASC")
    List<StudentTeacher> findAllActive();
    
    @Query("SELECT DISTINCT st FROM StudentTeacher st " +
           "LEFT JOIN FETCH st.user " +
           "LEFT JOIN FETCH st.createdBy")
    @Override
    List<StudentTeacher> findAll();
    
    @Query("SELECT DISTINCT st FROM StudentTeacher st " +
           "LEFT JOIN FETCH st.user " +
           "LEFT JOIN FETCH st.createdBy " +
           "WHERE st.user.id = :userId")
    java.util.Optional<StudentTeacher> findByUserId(@Param("userId") Long userId);
}

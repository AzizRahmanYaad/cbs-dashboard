package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Coordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, Long> {
    @Query("SELECT DISTINCT c FROM Coordinator c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.createdBy " +
           "WHERE c.isActive = true " +
           "ORDER BY c.user.username ASC")
    List<Coordinator> findAllActive();
    
    @Query("SELECT DISTINCT c FROM Coordinator c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.createdBy")
    @Override
    List<Coordinator> findAll();
    
    @Query("SELECT DISTINCT c FROM Coordinator c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.createdBy " +
           "WHERE c.user.id = :userId")
    java.util.Optional<Coordinator> findByUserId(@Param("userId") Long userId);
}

package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingModuleRepository extends JpaRepository<TrainingModule, Long> {
    @Query("SELECT DISTINCT tm FROM TrainingModule tm " +
           "LEFT JOIN FETCH tm.createdBy " +
           "WHERE tm.isActive = true " +
           "ORDER BY tm.name ASC")
    List<TrainingModule> findAllActive();
    
    @Query("SELECT DISTINCT tm FROM TrainingModule tm " +
           "LEFT JOIN FETCH tm.createdBy")
    @Override
    List<TrainingModule> findAll();
    
    boolean existsByName(String name);
}

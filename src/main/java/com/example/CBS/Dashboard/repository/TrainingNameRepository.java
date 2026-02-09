package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingNameRepository extends JpaRepository<TrainingName, Long> {
    @Query("SELECT DISTINCT tn FROM TrainingName tn " +
           "LEFT JOIN FETCH tn.createdBy " +
           "WHERE tn.isActive = true " +
           "ORDER BY tn.name ASC")
    List<TrainingName> findAllActive();
    
    @Query("SELECT DISTINCT tn FROM TrainingName tn " +
           "LEFT JOIN FETCH tn.createdBy")
    @Override
    List<TrainingName> findAll();
    
    boolean existsByName(String name);
}

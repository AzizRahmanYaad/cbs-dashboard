package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingTopicRepository extends JpaRepository<TrainingTopic, Long> {
    @Query("SELECT DISTINCT tt FROM TrainingTopic tt " +
           "LEFT JOIN FETCH tt.createdBy " +
           "WHERE tt.isActive = true " +
           "ORDER BY tt.name ASC")
    List<TrainingTopic> findAllActive();
    
    @Query("SELECT DISTINCT tt FROM TrainingTopic tt " +
           "LEFT JOIN FETCH tt.createdBy")
    @Override
    List<TrainingTopic> findAll();
    
    boolean existsByName(String name);
}

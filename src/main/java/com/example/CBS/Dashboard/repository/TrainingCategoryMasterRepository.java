package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingCategoryMasterRepository extends JpaRepository<TrainingCategoryMaster, Long> {
    @Query("SELECT DISTINCT tcm FROM TrainingCategoryMaster tcm " +
           "LEFT JOIN FETCH tcm.createdBy " +
           "WHERE tcm.isActive = true " +
           "ORDER BY tcm.name ASC")
    List<TrainingCategoryMaster> findAllActive();
    
    @Query("SELECT DISTINCT tcm FROM TrainingCategoryMaster tcm " +
           "LEFT JOIN FETCH tcm.createdBy")
    @Override
    List<TrainingCategoryMaster> findAll();
    
    boolean existsByName(String name);
}

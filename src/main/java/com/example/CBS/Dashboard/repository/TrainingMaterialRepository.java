package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {
    @Query("SELECT DISTINCT tm FROM TrainingMaterial tm " +
           "LEFT JOIN FETCH tm.program " +
           "LEFT JOIN FETCH tm.uploadedBy " +
           "ORDER BY tm.displayOrder DESC NULLS LAST, tm.id DESC")
    @Override
    List<TrainingMaterial> findAll();
    
    @Query("SELECT DISTINCT tm FROM TrainingMaterial tm " +
           "LEFT JOIN FETCH tm.program " +
           "LEFT JOIN FETCH tm.uploadedBy " +
           "WHERE tm.program.id = :programId " +
           "ORDER BY tm.displayOrder DESC NULLS LAST, tm.id DESC")
    List<TrainingMaterial> findByProgramId(@Param("programId") Long programId);
    
    @Query("SELECT DISTINCT tm FROM TrainingMaterial tm " +
           "LEFT JOIN FETCH tm.program " +
           "LEFT JOIN FETCH tm.uploadedBy " +
           "WHERE tm.materialType = :materialType")
    List<TrainingMaterial> findByMaterialType(@Param("materialType") String materialType);
}

package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TestModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestModuleRepository extends JpaRepository<TestModule, Long> {
    @Query("SELECT DISTINCT tm FROM TestModule tm " +
           "LEFT JOIN FETCH tm.createdBy")
    @Override
    List<TestModule> findAll();
    
    @Query("SELECT DISTINCT tm FROM TestModule tm " +
           "LEFT JOIN FETCH tm.createdBy " +
           "WHERE tm.createdBy.id = :createdById")
    List<TestModule> findByCreatedById(@Param("createdById") Long createdById);
}


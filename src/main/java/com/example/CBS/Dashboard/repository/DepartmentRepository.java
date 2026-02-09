package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT DISTINCT d FROM Department d " +
           "LEFT JOIN FETCH d.createdBy " +
           "WHERE d.isActive = true " +
           "ORDER BY d.name ASC")
    List<Department> findAllActive();
    
    @Query("SELECT DISTINCT d FROM Department d " +
           "LEFT JOIN FETCH d.createdBy")
    @Override
    List<Department> findAll();
    
    boolean existsByName(String name);
}

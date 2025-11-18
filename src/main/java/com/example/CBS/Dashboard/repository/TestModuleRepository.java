package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.TestModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestModuleRepository extends JpaRepository<TestModule, Long> {
    List<TestModule> findByCreatedById(Long createdById);
}


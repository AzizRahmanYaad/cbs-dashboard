package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.CreateTestModuleRequest;
import com.example.CBS.Dashboard.dto.test.TestModuleDto;
import com.example.CBS.Dashboard.entity.TestModule;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.mapper.TestModuleMapper;
import com.example.CBS.Dashboard.repository.TestModuleRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestModuleService {
    
    private final TestModuleRepository testModuleRepository;
    private final UserRepository userRepository;
    private final TestModuleMapper mapper;
    
    @Transactional
    public TestModuleDto createModule(CreateTestModuleRequest request, String username) {
        try {
            System.out.println("Creating module for user: " + username);
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            System.out.println("User found: " + user.getId() + " - " + user.getUsername());
            
            TestModule module = new TestModule();
            module.setName(request.getName());
            module.setDescription(request.getDescription());
            module.setCreatedBy(user);
            
            System.out.println("Saving module...");
            TestModule saved = testModuleRepository.save(module);
            System.out.println("Module saved with ID: " + saved.getId());
            
            // Force initialization of lazy-loaded relationships within transaction using Hibernate
            if (saved.getCreatedBy() != null) {
                Hibernate.initialize(saved.getCreatedBy());
                // Access to ensure it's loaded
                System.out.println("Created by user: " + saved.getCreatedBy().getId() + " - " + saved.getCreatedBy().getUsername());
            }
            
            System.out.println("Mapping to DTO...");
            TestModuleDto dto = mapper.toDto(saved);
            System.out.println("DTO created successfully");
            return dto;
        } catch (Exception e) {
            System.err.println("Error in createModule: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public List<TestModuleDto> getAllModules() {
        try {
            System.out.println("Getting all modules...");
            List<TestModule> modules = testModuleRepository.findAll();
            System.out.println("Found " + modules.size() + " modules");
            
            // Force initialization of lazy-loaded relationships within transaction using Hibernate
            modules.forEach(module -> {
                if (module.getCreatedBy() != null) {
                    try {
                        Hibernate.initialize(module.getCreatedBy());
                    } catch (Exception e) {
                        System.err.println("Error initializing createdBy for module " + module.getId() + ": " + e.getMessage());
                    }
                }
            });
            
            return modules.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllModules: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public TestModuleDto getModuleById(Long id) {
        TestModule module = testModuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Module not found with id: " + id));
        return mapper.toDto(module);
    }
    
    @Transactional
    public TestModuleDto updateModule(Long id, CreateTestModuleRequest request) {
        TestModule module = testModuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Module not found with id: " + id));
        
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        
        TestModule updated = testModuleRepository.save(module);
        return mapper.toDto(updated);
    }
    
    @Transactional
    public void deleteModule(Long id) {
        if (!testModuleRepository.existsById(id)) {
            throw new RuntimeException("Module not found with id: " + id);
        }
        testModuleRepository.deleteById(id);
    }
}


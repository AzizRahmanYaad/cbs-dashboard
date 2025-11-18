package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.test.TestModuleDto;
import com.example.CBS.Dashboard.entity.TestModule;
import org.springframework.stereotype.Component;

@Component
public class TestModuleMapper {
    
    public TestModuleDto toDto(TestModule module) {
        try {
            if (module == null) {
                System.out.println("TestModuleMapper: module is null");
                return null;
            }
            
            System.out.println("TestModuleMapper: Mapping module ID " + module.getId());
            TestModuleDto dto = new TestModuleDto();
            dto.setId(module.getId());
            dto.setName(module.getName());
            dto.setDescription(module.getDescription());
            
            // Safely access lazy-loaded createdBy relationship
            try {
                System.out.println("TestModuleMapper: Accessing createdBy...");
                if (module.getCreatedBy() != null) {
                    System.out.println("TestModuleMapper: createdBy is not null, getting ID and username...");
                    dto.setCreatedById(module.getCreatedBy().getId());
                    dto.setCreatedByUsername(module.getCreatedBy().getUsername());
                    System.out.println("TestModuleMapper: Successfully set createdBy: " + dto.getCreatedById() + " - " + dto.getCreatedByUsername());
                } else {
                    System.out.println("TestModuleMapper: createdBy is null");
                    dto.setCreatedById(null);
                    dto.setCreatedByUsername(null);
                }
            } catch (Exception e) {
                // Handle LazyInitializationException
                System.err.println("TestModuleMapper: Error accessing createdBy: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                dto.setCreatedById(null);
                dto.setCreatedByUsername(null);
            }
            
            dto.setCreatedAt(module.getCreatedAt());
            dto.setUpdatedAt(module.getUpdatedAt());
            System.out.println("TestModuleMapper: DTO mapping completed successfully");
            return dto;
        } catch (Exception e) {
            System.err.println("TestModuleMapper: Fatal error in toDto: " + e.getClass().getName());
            System.err.println("TestModuleMapper: Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}


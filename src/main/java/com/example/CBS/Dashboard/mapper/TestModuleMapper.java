package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.test.TestModuleDto;
import com.example.CBS.Dashboard.entity.TestModule;
import org.springframework.stereotype.Component;

@Component
public class TestModuleMapper {
    
    public TestModuleDto toDto(TestModule module) {
        if (module == null) return null;
        
        TestModuleDto dto = new TestModuleDto();
        dto.setId(module.getId());
        dto.setName(module.getName());
        dto.setDescription(module.getDescription());
        
        // Safely access lazy-loaded createdBy relationship
        try {
            if (module.getCreatedBy() != null) {
                dto.setCreatedById(module.getCreatedBy().getId());
                dto.setCreatedByUsername(module.getCreatedBy().getUsername());
            } else {
                dto.setCreatedById(null);
                dto.setCreatedByUsername(null);
            }
        } catch (Exception e) {
            // Handle LazyInitializationException
            dto.setCreatedById(null);
            dto.setCreatedByUsername(null);
        }
        
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        return dto;
    }
}


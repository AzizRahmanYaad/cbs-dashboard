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
        dto.setCreatedById(module.getCreatedBy() != null ? module.getCreatedBy().getId() : null);
        dto.setCreatedByUsername(module.getCreatedBy() != null ? module.getCreatedBy().getUsername() : null);
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        return dto;
    }
}


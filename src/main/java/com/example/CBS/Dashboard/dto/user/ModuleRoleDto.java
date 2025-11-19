package com.example.CBS.Dashboard.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleRoleDto {
    private String moduleName;
    private String moduleDisplayName;
    private List<RoleDto> roles;
}

package com.example.CBS.Dashboard.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private Set<String> roles;
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
}

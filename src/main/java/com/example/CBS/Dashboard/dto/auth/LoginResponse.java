package com.example.CBS.Dashboard.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private Long expiresIn;
    
    @Builder.Default
    private String tokenType = "Bearer";
}

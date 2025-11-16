package com.example.CBS.Dashboard.controller.auth;

import com.example.CBS.Dashboard.dto.auth.LoginRequest;
import com.example.CBS.Dashboard.dto.auth.LoginResponse;
import com.example.CBS.Dashboard.dto.auth.RefreshTokenRequest;
import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.service.auth.AuthService;
import com.example.CBS.Dashboard.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDto userDto = userService.getUserProfile(username);
        return ResponseEntity.ok(userDto);
    }
}

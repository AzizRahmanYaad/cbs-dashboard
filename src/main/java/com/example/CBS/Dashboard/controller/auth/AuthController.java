package com.example.CBS.Dashboard.controller.auth;

import com.example.CBS.Dashboard.dto.auth.LoginRequest;
import com.example.CBS.Dashboard.dto.auth.LoginResponse;
import com.example.CBS.Dashboard.dto.auth.RefreshTokenRequest;
import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.service.auth.AuthService;
import com.example.CBS.Dashboard.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login request received for username: {}", loginRequest.getUsername());
            LoginResponse response = authService.login(loginRequest);
            logger.info("Login successful for username: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Invalid username or password\"}");
        } catch (Exception e) {
            logger.error("Unexpected error during login for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"An unexpected error occurred during login\"}");
        }
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

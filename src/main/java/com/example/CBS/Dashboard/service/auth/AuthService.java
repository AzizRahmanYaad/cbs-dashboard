package com.example.CBS.Dashboard.service.auth;

import com.example.CBS.Dashboard.dto.auth.LoginRequest;
import com.example.CBS.Dashboard.dto.auth.LoginResponse;
import com.example.CBS.Dashboard.exception.InvalidRefreshTokenException;
import com.example.CBS.Dashboard.security.JwtTokenProvider;
import com.example.CBS.Dashboard.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserService userService;
    
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());
            
            log.info("Successful login for username: {}", loginRequest.getUsername());
            
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(tokenProvider.getExpirationTime())
                    .tokenType("Bearer")
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", loginRequest.getUsername());
            throw e;
        }
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        log.debug("Token refresh attempt");
        
        if (!tokenProvider.validateToken(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }
        
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        log.debug("Token refresh for username: {}", username);
        
        var user = userService.findByUsername(username);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(
                        user.getRoles().stream()
                                .map(role -> role.getName())
                                .toArray(String[]::new)
                )
        );
        
        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);
        
        log.info("Token refreshed successfully for username: {}", username);
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .tokenType("Bearer")
                .build();
    }
}

package com.example.CBS.Dashboard.service.user;

import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        log.debug("Fetching user profile for username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        
        return convertToDto(user);
    }
    
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

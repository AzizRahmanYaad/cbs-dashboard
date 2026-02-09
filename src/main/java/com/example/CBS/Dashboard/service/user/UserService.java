package com.example.CBS.Dashboard.service.user;

import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.mapper.UserMapper;
import com.example.CBS.Dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return userMapper.toDto(user);
    }

    @Transactional
    public void saveSignature(String username, String signatureData) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        String cleanData = signatureData;
        if (cleanData != null && cleanData.contains(",")) {
            cleanData = cleanData.substring(cleanData.indexOf(",") + 1).trim();
        }
        user.setSignatureData(cleanData);
        user.setSignatureCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String getSignatureData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return user.getSignatureData();
    }
    
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
}

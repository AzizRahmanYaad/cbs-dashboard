package com.example.CBS.Dashboard.service.user;

import com.example.CBS.Dashboard.dto.user.CreateUserRequest;
import com.example.CBS.Dashboard.dto.user.RoleDto;
import com.example.CBS.Dashboard.dto.user.UpdateUserRequest;
import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.entity.Role;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.mapper.UserMapper;
import com.example.CBS.Dashboard.repository.RoleRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private static final String ROLE_DESCRIPTION_SUFFIX = " module access";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAvailableRoles() {
        return roleRepository.findAllByOrderByNameAsc()
                .stream()
                .map(role -> new RoleDto(role.getName(), buildRoleDescription(role.getName())))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Set<Role> roles = fetchRoles(request.getRoles());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
        user.setRoles(roles);

        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = fetchRoles(request.getRoles());
            user.setRoles(roles);
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return userMapper.toDto(userRepository.save(user));
    }

    private Set<Role> fetchRoles(Set<String> roleNames) {
        List<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new IllegalArgumentException("One or more roles are invalid");
        }
        return roles.stream().collect(Collectors.toSet());
    }

    private String buildRoleDescription(String roleName) {
        if (roleName.equalsIgnoreCase("ROLE_ADMIN")) {
            return "Full administrative access";
        }
        if (roleName.equalsIgnoreCase("ROLE_USER")) {
            return "Basic dashboard access";
        }
        if (roleName.startsWith("ROLE_")) {
            String module = roleName.replace("ROLE_", "").replace("_", " ").toLowerCase();
            module = Character.toUpperCase(module.charAt(0)) + module.substring(1);
            return module + ROLE_DESCRIPTION_SUFFIX;
        }
        return "Application role";
    }
}


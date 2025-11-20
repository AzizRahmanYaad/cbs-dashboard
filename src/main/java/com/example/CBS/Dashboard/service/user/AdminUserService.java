package com.example.CBS.Dashboard.service.user;

import com.example.CBS.Dashboard.dto.user.CreateUserRequest;
import com.example.CBS.Dashboard.dto.user.ModuleRoleDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
                .map(role -> {
                    String module = extractModuleFromRole(role.getName());
                    return new RoleDto(role.getName(), buildRoleDescription(role.getName()), module);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ModuleRoleDto> getRolesByModule() {
        Map<String, List<RoleDto>> moduleMap = new LinkedHashMap<>();
        
        // Define module display order
        List<String> moduleOrder = Arrays.asList("ADMIN", "GENERAL", "DRILL", "TRAINING", "DAILY", "TEST_MANAGEMENT", "MANAGER", "OTHER");
        
        roleRepository.findAllByOrderByNameAsc().forEach(role -> {
            String module = extractModuleFromRole(role.getName());
            String moduleDisplayName = getModuleDisplayName(module);
            
            moduleMap.computeIfAbsent(module, k -> new ArrayList<>())
                    .add(new RoleDto(role.getName(), buildRoleDescription(role.getName()), module));
        });
        
        // Sort modules according to predefined order
        return moduleOrder.stream()
                .filter(moduleMap::containsKey)
                .map(module -> new ModuleRoleDto(
                        module,
                        getModuleDisplayName(module),
                        moduleMap.get(module)
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable, String search) {
        Specification<User> spec = Specification.where(null);
        
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("username")), searchTerm),
                    cb.like(cb.lower(root.get("email")), searchTerm)
                )
            );
        }
        
        return userRepository.findAll(spec, pageable)
                .map(userMapper::toDto);
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
        
        // Daily Report specific roles
        if (roleName.equalsIgnoreCase("ROLE_DAILY_REPORT_EMPLOYEE")) {
            return "Can create and submit own daily reports";
        }
        if (roleName.equalsIgnoreCase("ROLE_DAILY_REPORT_SUPERVISOR")) {
            return "Can review, approve, or send back daily reports";
        }
        if (roleName.equalsIgnoreCase("ROLE_DAILY_REPORT_DIRECTOR")) {
            return "View-only or full access to daily reports";
        }
        if (roleName.equalsIgnoreCase("ROLE_DAILY_REPORT_MANAGER")) {
            return "View-only or full access to daily reports";
        }
        if (roleName.equalsIgnoreCase("ROLE_DAILY_REPORT_TEAM_LEAD")) {
            return "View-only or full access to daily reports";
        }
        if (roleName.equalsIgnoreCase("ROLE_DAILY_REPORT")) {
            return "General daily report module access";
        }
        
        if (roleName.startsWith("ROLE_")) {
            String module = roleName.replace("ROLE_", "").replace("_", " ").toLowerCase();
            module = Character.toUpperCase(module.charAt(0)) + module.substring(1);
            return module + ROLE_DESCRIPTION_SUFFIX;
        }
        return "Application role";
    }

    private String extractModuleFromRole(String roleName) {
        if (roleName.equalsIgnoreCase("ROLE_ADMIN")) {
            return "ADMIN";
        }
        if (roleName.equalsIgnoreCase("ROLE_USER")) {
            return "GENERAL";
        }
        if (roleName.startsWith("ROLE_")) {
            String rolePart = roleName.replace("ROLE_", "").toUpperCase();
            
            // Handle specific role mappings
            if (rolePart.equals("DRILL_TESTING") || rolePart.startsWith("DRILL")) {
                return "DRILL";
            }
            if (rolePart.equals("TRAINING")) {
                return "TRAINING";
            }
            if (rolePart.equals("DAILY_REPORT") || rolePart.startsWith("DAILY")) {
                return "DAILY";
            }
            if (rolePart.equals("QA_LEAD") || rolePart.equals("TESTER") || rolePart.startsWith("QA") || rolePart.startsWith("TEST")) {
                return "TEST_MANAGEMENT";
            }
            if (rolePart.equals("MANAGER")) {
                return "MANAGER";
            }
            
            // Handle compound roles
            if (rolePart.contains("_")) {
                String[] parts = rolePart.split("_");
                return parts[0];
            }
            
            return rolePart;
        }
        return "OTHER";
    }

    private String getModuleDisplayName(String module) {
        Map<String, String> moduleNames = new HashMap<>();
        moduleNames.put("ADMIN", "Administration");
        moduleNames.put("GENERAL", "General Access");
        moduleNames.put("DRILL", "Drill Test Module");
        moduleNames.put("TRAINING", "Training Module");
        moduleNames.put("DAILY", "Daily Report Module");
        moduleNames.put("TEST_MANAGEMENT", "Test Management Module");
        moduleNames.put("QA", "Test Management Module");
        moduleNames.put("TESTER", "Test Management Module");
        moduleNames.put("MANAGER", "Management Module");
        moduleNames.put("OTHER", "Other Modules");
        
        return moduleNames.getOrDefault(module, module.replace("_", " "));
    }
}


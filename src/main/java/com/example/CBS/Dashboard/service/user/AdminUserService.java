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
import com.example.CBS.Dashboard.repository.StudentTeacherRepository;
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

    @Autowired
    private StudentTeacherRepository studentTeacherRepository;

    /**
     * Users that are not yet assigned as student or teacher (no row in student_teachers).
     * Excludes users who already have any student_teachers record and optionally ADMIN.
     * Use for dropdown when creating a new student or teacher.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersAvailableForTraining() {
        Set<Long> assignedUserIds = new HashSet<>(studentTeacherRepository.findAllAssignedUserIds());
        return userRepository.findAll()
                .stream()
                .filter(user -> !assignedUserIds.contains(user.getId()))
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> "ROLE_ADMIN".equals(role.getName())))
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public List<ModuleRoleDto> getRolesByModule() {
        try {
            ensureCfoRoleExists();
        } catch (Exception e) {
            System.err.println("ensureCfoRoleExists failed (CFO role will still appear in list): " + e.getMessage());
        }
        Map<String, List<RoleDto>> moduleMap = new LinkedHashMap<>();
        
        // Define module display order
        List<String> moduleOrder = Arrays.asList("ADMIN", "GENERAL", "CFO", "DRILL", "TRAINING", "DAILY", "TEST_MANAGEMENT", "MANAGER", "OTHER");
        
        // Get all roles from database
        List<Role> allRoles = roleRepository.findAllByOrderByNameAsc();
        
        allRoles.forEach(role -> {
            String module = extractModuleFromRole(role.getName());
            moduleMap.computeIfAbsent(module, k -> new ArrayList<>())
                    .add(new RoleDto(role.getName(), buildRoleDescription(role.getName()), module));
        });
        
        // Ensure CFO module always has ROLE_CFO (in case it was missing from DB or mis-mapped)
        ensureCfoModuleInMap(moduleMap);
        
        // Sort modules according to predefined order
        List<ModuleRoleDto> result = moduleOrder.stream()
                .filter(moduleMap::containsKey)
                .map(module -> new ModuleRoleDto(
                        module,
                        getModuleDisplayName(module),
                        moduleMap.get(module)
                ))
                .collect(Collectors.toList());
        
        // Guarantee CFO module is in the response (insert at correct position if missing)
        boolean hasCfoModule = result.stream().anyMatch(mr -> "CFO".equals(mr.getModuleName()));
        if (!hasCfoModule) {
            List<RoleDto> cfoRoleList = new ArrayList<>();
            cfoRoleList.add(new RoleDto("ROLE_CFO", buildRoleDescription("ROLE_CFO"), "CFO"));
            ModuleRoleDto cfoModule = new ModuleRoleDto("CFO", getModuleDisplayName("CFO"), cfoRoleList);
            int insertIndex = Math.min(2, result.size());
            result.add(insertIndex, cfoModule);
        }
        
        return result;
    }

    /** Ensures ROLE_CFO exists in the database so it can be assigned in User Management. */
    private void ensureCfoRoleExists() {
        roleRepository.findByName("ROLE_CFO")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CFO")));
    }

    /** Ensures the CFO module is present in the map with ROLE_CFO, so it always appears in User Management. */
    private void ensureCfoModuleInMap(Map<String, List<RoleDto>> moduleMap) {
        moduleMap.putIfAbsent("CFO", new ArrayList<>());
        List<RoleDto> cfoRoles = moduleMap.get("CFO");
        boolean hasCfo = cfoRoles.stream().anyMatch(r -> "ROLE_CFO".equals(r.getName()));
        if (!hasCfo) {
            cfoRoles.add(new RoleDto("ROLE_CFO", buildRoleDescription("ROLE_CFO"), "CFO"));
        }
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
        user.setFullName(request.getFullName());
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

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

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
        
        // Training specific roles
        if (roleName.equalsIgnoreCase("ROLE_TEACHER")) {
            return "Teacher - Access to teacher dashboard (manage programs, sessions, materials, students, attendance)";
        }
        if (roleName.equalsIgnoreCase("ROLE_STUDENT")) {
            return "Student - Access to student dashboard (view enrolled programs, sessions, materials, attendance, progress)";
        }
        if (roleName.equalsIgnoreCase("ROLE_TRAINING_ADMIN")) {
            return "Training Administrator - Full access to training module administration";
        }
        if (roleName.equalsIgnoreCase("ROLE_TRAINING")) {
            return "Training Module - General training module access";
        }
        
        // Daily Report specific roles
        if (roleName.equalsIgnoreCase("ROLE_INDIVIDUAL_REPORT")) {
            return "Full access to own daily reports (create, edit, view, download)";
        }
        if (roleName.equalsIgnoreCase("ROLE_QUALITY_CONTROL")) {
            return "Quality Control - View, review, and manage all submitted individual reports";
        }
        if (roleName.equalsIgnoreCase("ROLE_CFO")) {
            return "CFO - View-only access to all modules, reports, dashboards, and analytics; filter, search, export (PDF/Excel); no create/edit/delete";
        }
        
        if (roleName.startsWith("ROLE_")) {
            String module = roleName.replace("ROLE_", "").replace("_", " ").toLowerCase();
            module = Character.toUpperCase(module.charAt(0)) + module.substring(1);
            return module + ROLE_DESCRIPTION_SUFFIX;
        }
        return "Application role";
    }

    private String extractModuleFromRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return "OTHER";
        }
        
        // Normalize role name
        String normalizedRoleName = roleName.trim();
        
        if (normalizedRoleName.equalsIgnoreCase("ROLE_ADMIN")) {
            return "ADMIN";
        }
        if (normalizedRoleName.equalsIgnoreCase("ROLE_USER")) {
            return "GENERAL";
        }
        if (normalizedRoleName.startsWith("ROLE_")) {
            String rolePart = normalizedRoleName.replace("ROLE_", "").toUpperCase().trim();
            
            // Handle specific role mappings - check exact matches first (most specific first)
            if (rolePart.equals("TEACHER")) {
                return "TRAINING";
            }
            if (rolePart.equals("STUDENT")) {
                return "TRAINING";
            }
            if (rolePart.equals("TRAINING_ADMIN")) {
                return "TRAINING";
            }
            if (rolePart.equals("INDIVIDUAL_REPORT")) {
                return "DAILY";
            }
            if (rolePart.equals("QUALITY_CONTROL")) {
                return "DAILY";
            }
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
            if (rolePart.equals("CFO")) {
                return "CFO";
            }
            
            // Handle compound roles - check if it contains REPORT or QUALITY for Daily Report module
            // But exclude REPORT_ADMIN to avoid conflicts
            if (rolePart.contains("_")) {
                String[] parts = rolePart.split("_");
                // If the role contains "REPORT" but is not "REPORT_ADMIN", it's likely a Daily Report role
                if (rolePart.contains("REPORT") && !rolePart.equals("REPORT_ADMIN")) {
                    return "DAILY";
                }
                // If the role contains "QUALITY", it's likely a Daily Report Quality Control role
                if (rolePart.contains("QUALITY")) {
                    return "DAILY";
                }
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
        moduleNames.put("CFO", "CFO (View-Only Executive Access)");
        moduleNames.put("OTHER", "Other Modules");
        
        return moduleNames.getOrDefault(module, module.replace("_", " "));
    }
}


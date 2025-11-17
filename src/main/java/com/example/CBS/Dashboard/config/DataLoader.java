package com.example.CBS.Dashboard.config;

import com.example.CBS.Dashboard.entity.Role;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.RoleRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        List<String> defaultRoles = List.of(
                "ROLE_ADMIN",
                "ROLE_USER",
                "ROLE_TRAINING",
                "ROLE_DRILL_TESTING",
                "ROLE_DAILY_REPORT"
        );
        defaultRoles.forEach(roleName -> roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName))));
        
        if (userRepository.count() == 0) {
            Set<Role> roles = new HashSet<>(roleRepository.findAll());
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@cbsdashboard.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            
            System.out.println("================================");
            System.out.println("Admin user created with default credentials.");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Please change this password immediately.");
            System.out.println("================================");
        }
    }
}

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
        if (roleRepository.count() == 0) {
            Role adminRole = new Role("ROLE_ADMIN");
            Role userRole = new Role("ROLE_USER");
            
            roleRepository.save(adminRole);
            roleRepository.save(userRole);
        }
        
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@cbsdashboard.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            
            System.out.println("================================");
            System.out.println("Admin user created successfully!");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("================================");
        }
    }
}

package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.CreateTestModuleRequest;
import com.example.CBS.Dashboard.dto.test.TestModuleDto;
import com.example.CBS.Dashboard.entity.TestModule;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.mapper.TestModuleMapper;
import com.example.CBS.Dashboard.repository.TestModuleRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestModuleService {
    
    private final TestModuleRepository testModuleRepository;
    private final UserRepository userRepository;
    private final TestModuleMapper mapper;
    
    @Transactional
    public TestModuleDto createModule(CreateTestModuleRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TestModule module = new TestModule();
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        module.setCreatedBy(user);
        
        TestModule saved = testModuleRepository.save(module);
        
        // Force initialization of lazy-loaded relationships within transaction
        if (saved.getCreatedBy() != null) {
            // Access the relationship to force initialization
            saved.getCreatedBy().getId();
            saved.getCreatedBy().getUsername();
        }
        
        return mapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TestModuleDto> getAllModules() {
        List<TestModule> modules = testModuleRepository.findAll();
        
        // Force initialization of lazy-loaded relationships within transaction
        modules.forEach(module -> {
            if (module.getCreatedBy() != null) {
                try {
                    module.getCreatedBy().getId();
                    module.getCreatedBy().getUsername();
                } catch (Exception e) {
                    // Ignore if already initialized or not available
                }
            }
        });
        
        return modules.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TestModuleDto getModuleById(Long id) {
        TestModule module = testModuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Module not found with id: " + id));
        return mapper.toDto(module);
    }
    
    @Transactional
    public TestModuleDto updateModule(Long id, CreateTestModuleRequest request) {
        TestModule module = testModuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Module not found with id: " + id));
        
        module.setName(request.getName());
        module.setDescription(request.getDescription());
        
        TestModule updated = testModuleRepository.save(module);
        return mapper.toDto(updated);
    }
    
    @Transactional
    public void deleteModule(Long id) {
        if (!testModuleRepository.existsById(id)) {
            throw new RuntimeException("Module not found with id: " + id);
        }
        testModuleRepository.deleteById(id);
    }
}


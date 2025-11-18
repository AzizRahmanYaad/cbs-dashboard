package com.example.CBS.Dashboard.controller.test;

import com.example.CBS.Dashboard.dto.test.CreateTestModuleRequest;
import com.example.CBS.Dashboard.dto.test.TestModuleDto;
import com.example.CBS.Dashboard.service.test.TestModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/modules")
@RequiredArgsConstructor
public class TestModuleController {
    
    private final TestModuleService testModuleService;
    
    @PostMapping
    public ResponseEntity<TestModuleDto> createModule(
            @Valid @RequestBody CreateTestModuleRequest request,
            Authentication authentication) {
        TestModuleDto module = testModuleService.createModule(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(module);
    }
    
    @GetMapping
    public ResponseEntity<List<TestModuleDto>> getAllModules() {
        List<TestModuleDto> modules = testModuleService.getAllModules();
        return ResponseEntity.ok(modules);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestModuleDto> getModuleById(@PathVariable Long id) {
        TestModuleDto module = testModuleService.getModuleById(id);
        return ResponseEntity.ok(module);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestModuleDto> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody CreateTestModuleRequest request) {
        TestModuleDto module = testModuleService.updateModule(id, request);
        return ResponseEntity.ok(module);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        testModuleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}


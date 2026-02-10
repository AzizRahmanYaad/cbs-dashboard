package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.dto.training.CreateTrainingMaterialRequest;
import com.example.CBS.Dashboard.dto.training.TrainingMaterialDto;
import com.example.CBS.Dashboard.service.training.TrainingMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training/materials")
@RequiredArgsConstructor
public class TrainingMaterialController {
    
    private final TrainingMaterialService materialService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingMaterialDto> createMaterial(
            @Valid @RequestBody CreateTrainingMaterialRequest request,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        String username = authentication.getName();
        TrainingMaterialDto material = materialService.createMaterial(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(material);
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingMaterialDto>> getMaterials(
            @RequestParam(required = false) Long programId,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<TrainingMaterialDto> materials;
        if (programId != null) {
            materials = materialService.getMaterialsByProgram(programId, authentication.getName());
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(materials);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrainingMaterialDto> getMaterialById(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TrainingMaterialDto material = materialService.getMaterialById(id, authentication.getName());
        return ResponseEntity.ok(material);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<TrainingMaterialDto> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody CreateTrainingMaterialRequest request) {
        TrainingMaterialDto material = materialService.updateMaterial(id, request);
        return ResponseEntity.ok(material);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_TRAINING_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}

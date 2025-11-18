package com.example.CBS.Dashboard.controller.test;

import com.example.CBS.Dashboard.dto.test.CreateDefectRequest;
import com.example.CBS.Dashboard.dto.test.DefectDto;
import com.example.CBS.Dashboard.dto.test.UpdateDefectRequest;
import com.example.CBS.Dashboard.entity.Defect;
import com.example.CBS.Dashboard.service.test.DefectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/defects")
@RequiredArgsConstructor
public class DefectController {
    
    private final DefectService defectService;
    
    @PostMapping
    public ResponseEntity<DefectDto> createDefect(
            @Valid @RequestBody CreateDefectRequest request,
            Authentication authentication) {
        DefectDto defect = defectService.createDefect(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(defect);
    }
    
    @GetMapping
    public ResponseEntity<List<DefectDto>> getAllDefects(
            @RequestParam(required = false) Defect.DefectStatus status,
            @RequestParam(required = false) Defect.DefectSeverity severity,
            @RequestParam(required = false) Long assignedToId) {
        List<DefectDto> defects;
        if (status != null || severity != null || assignedToId != null) {
            defects = defectService.getDefectsByFilters(status, severity, assignedToId);
        } else {
            defects = defectService.getAllDefects();
        }
        return ResponseEntity.ok(defects);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DefectDto> getDefectById(@PathVariable Long id) {
        DefectDto defect = defectService.getDefectById(id);
        return ResponseEntity.ok(defect);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DefectDto> updateDefect(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDefectRequest request,
            Authentication authentication) {
        DefectDto defect = defectService.updateDefect(id, request, authentication.getName());
        return ResponseEntity.ok(defect);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDefect(
            @PathVariable Long id,
            Authentication authentication) {
        defectService.deleteDefect(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}


package com.example.CBS.Dashboard.controller.training;

import com.example.CBS.Dashboard.service.training.MaterialReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Endpoints for tracking when a user has reviewed / followed training materials.
 * This is used to provide an audit trail of material engagement whenever links
 * are opened from the Teacher or Student dashboards.
 */
@RestController
@RequestMapping("/api/training/materials")
@RequiredArgsConstructor
public class MaterialReviewController {

    private final MaterialReviewService materialReviewService;

    /**
     * Mark the given material as reviewed by the currently authenticated user.
     * This call is idempotent – repeat calls for the same user/material pair
     * are ignored by the service.
     */
    @PostMapping("/{materialId}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> confirmReview(
            @PathVariable Long materialId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        materialReviewService.confirmMaterialReview(authentication.getName(), materialId);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the IDs of all materials that the current user has already reviewed.
     * Can be used by frontends to show completion ticks on material lists.
     */
    @GetMapping("/reviewed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Set<Long>> getReviewedMaterials(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Set<Long> reviewed = materialReviewService.getReviewedMaterialIds(authentication.getName());
        return ResponseEntity.ok(reviewed);
    }
}


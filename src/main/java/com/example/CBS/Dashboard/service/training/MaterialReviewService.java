package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.entity.MaterialReview;
import com.example.CBS.Dashboard.entity.TrainingMaterial;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.MaterialReviewRepository;
import com.example.CBS.Dashboard.repository.TrainingMaterialRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialReviewService {

    private final MaterialReviewRepository materialReviewRepository;
    private final TrainingMaterialRepository materialRepository;
    private final UserRepository userRepository;

    /**
     * Records that the current user has reviewed a material (e-signed with their profile).
     * Requires the user to have a registered signature.
     */
    @Transactional
    public void confirmMaterialReview(String username, Long materialId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (user.getSignatureData() == null || user.getSignatureData().isBlank()) {
            throw new IllegalStateException("You must register your e-signature before confirming material review");
        }
        TrainingMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        if (materialReviewRepository.existsByUserIdAndMaterialId(user.getId(), materialId)) {
            return; // Already reviewed
        }
        MaterialReview review = new MaterialReview();
        review.setUser(user);
        review.setMaterial(material);
        materialReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public java.util.Set<Long> getReviewedMaterialIds(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return materialReviewRepository.findByUserId(user.getId()).stream()
                .map(r -> r.getMaterial().getId())
                .collect(java.util.stream.Collectors.toSet());
    }
}

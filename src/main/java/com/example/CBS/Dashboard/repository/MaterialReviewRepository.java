package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.MaterialReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialReviewRepository extends JpaRepository<MaterialReview, Long> {

    Optional<MaterialReview> findByUserIdAndMaterialId(Long userId, Long materialId);

    boolean existsByUserIdAndMaterialId(Long userId, Long materialId);

    List<MaterialReview> findByUserId(Long userId);
}

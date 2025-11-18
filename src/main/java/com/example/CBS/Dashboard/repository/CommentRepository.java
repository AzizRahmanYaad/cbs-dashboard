package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTestCaseIdOrderByCreatedAtDesc(Long testCaseId);
    List<Comment> findByDefectIdOrderByCreatedAtDesc(Long defectId);
    List<Comment> findByCreatedById(Long createdById);
}


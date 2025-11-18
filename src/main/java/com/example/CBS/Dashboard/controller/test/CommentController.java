package com.example.CBS.Dashboard.controller.test;

import com.example.CBS.Dashboard.dto.test.CommentDto;
import com.example.CBS.Dashboard.dto.test.CreateCommentRequest;
import com.example.CBS.Dashboard.service.test.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        CommentDto comment = commentService.createComment(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    @GetMapping("/test-case/{testCaseId}")
    public ResponseEntity<List<CommentDto>> getCommentsByTestCase(@PathVariable Long testCaseId) {
        List<CommentDto> comments = commentService.getCommentsByTestCaseId(testCaseId);
        return ResponseEntity.ok(comments);
    }
    
    @GetMapping("/defect/{defectId}")
    public ResponseEntity<List<CommentDto>> getCommentsByDefect(@PathVariable Long defectId) {
        List<CommentDto> comments = commentService.getCommentsByDefectId(defectId);
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}


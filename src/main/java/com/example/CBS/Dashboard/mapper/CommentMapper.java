package com.example.CBS.Dashboard.mapper;

import com.example.CBS.Dashboard.dto.test.CommentDto;
import com.example.CBS.Dashboard.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    
    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;
        
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedById(comment.getCreatedBy() != null ? comment.getCreatedBy().getId() : null);
        dto.setCreatedByUsername(comment.getCreatedBy() != null ? comment.getCreatedBy().getUsername() : null);
        dto.setTestCaseId(comment.getTestCase() != null ? comment.getTestCase().getId() : null);
        dto.setDefectId(comment.getDefect() != null ? comment.getDefect().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}


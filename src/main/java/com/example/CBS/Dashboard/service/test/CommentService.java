package com.example.CBS.Dashboard.service.test;

import com.example.CBS.Dashboard.dto.test.CommentDto;
import com.example.CBS.Dashboard.dto.test.CreateCommentRequest;
import com.example.CBS.Dashboard.entity.*;
import com.example.CBS.Dashboard.mapper.CommentMapper;
import com.example.CBS.Dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final TestCaseRepository testCaseRepository;
    private final DefectRepository defectRepository;
    private final UserRepository userRepository;
    private final CommentMapper mapper;
    
    @Transactional
    public CommentDto createComment(CreateCommentRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedBy(user);
        
        if (request.getTestCaseId() != null) {
            TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new RuntimeException("Test case not found with id: " + request.getTestCaseId()));
            comment.setTestCase(testCase);
        }
        
        if (request.getDefectId() != null) {
            Defect defect = defectRepository.findById(request.getDefectId())
                .orElseThrow(() -> new RuntimeException("Defect not found with id: " + request.getDefectId()));
            comment.setDefect(defect);
        }
        
        Comment saved = commentRepository.save(comment);
        return mapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByTestCaseId(Long testCaseId) {
        return commentRepository.findByTestCaseIdOrderByCreatedAtDesc(testCaseId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByDefectId(Long defectId) {
        return commentRepository.findByDefectIdOrderByCreatedAtDesc(defectId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }
}


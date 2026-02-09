package com.example.CBS.Dashboard.service.training;

import com.example.CBS.Dashboard.dto.training.CreateTrainingMaterialRequest;
import com.example.CBS.Dashboard.dto.training.TrainingMaterialDto;
import com.example.CBS.Dashboard.entity.TrainingMaterial;
import com.example.CBS.Dashboard.entity.TrainingProgram;
import com.example.CBS.Dashboard.entity.User;
import com.example.CBS.Dashboard.repository.TrainingMaterialRepository;
import com.example.CBS.Dashboard.repository.TrainingProgramRepository;
import com.example.CBS.Dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {
    
    private final TrainingMaterialRepository materialRepository;
    private final TrainingProgramRepository programRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public TrainingMaterialDto createMaterial(CreateTrainingMaterialRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        TrainingProgram program = programRepository.findById(request.getProgramId())
            .orElseThrow(() -> new RuntimeException("Training program not found"));
        
        TrainingMaterial material = new TrainingMaterial();
        material.setProgram(program);
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setMaterialType(request.getMaterialType());
        material.setFilePath(request.getFilePath());
        material.setFileSize(request.getFileSize());
        material.setFileName(request.getFileName());
        material.setIsRequired(request.getIsRequired() != null ? request.getIsRequired() : false);
        material.setDisplayOrder(request.getDisplayOrder());
        material.setUploadedBy(user);
        
        TrainingMaterial saved = materialRepository.save(material);
        return mapToDto(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TrainingMaterialDto> getMaterialsByProgram(Long programId) {
        List<TrainingMaterialDto> materials = materialRepository.findByProgramId(programId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        // Auto-generate display order in descending order
        assignDisplayOrder(materials);
        return materials;
    }
    
    private void assignDisplayOrder(List<TrainingMaterialDto> materials) {
        int sequence = materials.size();
        for (TrainingMaterialDto material : materials) {
            material.setDisplayOrder(sequence--);
        }
    }
    
    @Transactional(readOnly = true)
    public TrainingMaterialDto getMaterialById(Long id) {
        TrainingMaterial material = materialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training material not found"));
        return mapToDto(material);
    }
    
    @Transactional
    public TrainingMaterialDto updateMaterial(Long id, CreateTrainingMaterialRequest request) {
        TrainingMaterial material = materialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training material not found"));
        
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setMaterialType(request.getMaterialType());
        if (request.getFilePath() != null) {
            material.setFilePath(request.getFilePath());
        }
        if (request.getFileSize() != null) {
            material.setFileSize(request.getFileSize());
        }
        if (request.getFileName() != null) {
            material.setFileName(request.getFileName());
        }
        if (request.getIsRequired() != null) {
            material.setIsRequired(request.getIsRequired());
        }
        if (request.getDisplayOrder() != null) {
            material.setDisplayOrder(request.getDisplayOrder());
        }
        
        TrainingMaterial saved = materialRepository.save(material);
        return mapToDto(saved);
    }
    
    @Transactional
    public void deleteMaterial(Long id) {
        TrainingMaterial material = materialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Training material not found"));
        materialRepository.delete(material);
    }
    
    private TrainingMaterialDto mapToDto(TrainingMaterial material) {
        TrainingMaterialDto dto = new TrainingMaterialDto();
        dto.setId(material.getId());
        dto.setProgramId(material.getProgram().getId());
        dto.setProgramTitle(material.getProgram().getTitle());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setMaterialType(material.getMaterialType());
        dto.setFilePath(material.getFilePath());
        dto.setFileSize(material.getFileSize());
        dto.setFileName(material.getFileName());
        dto.setIsRequired(material.getIsRequired());
        dto.setDisplayOrder(material.getDisplayOrder());
        dto.setUploadedById(material.getUploadedBy().getId());
        dto.setUploadedByUsername(material.getUploadedBy().getUsername());
        dto.setCreatedAt(material.getCreatedAt());
        dto.setUpdatedAt(material.getUpdatedAt());
        return dto;
    }
}

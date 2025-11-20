package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCapacityBuildingDto {
    private Long id;
    private String trainingType;
    private String topic;
    private String duration;
    private String skillsGained;
    private String trainerName;
    private String participants;
}


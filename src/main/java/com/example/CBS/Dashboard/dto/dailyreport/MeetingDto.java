package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private String meetingType;
    private String topic;
    private String summary;
    private String actionTaken;
    private String nextStep;
    private String participants;
}


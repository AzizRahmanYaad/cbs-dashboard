package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCommunicationDto {
    private Long id;
    private String platform;
    private String summary;
    private String actionTaken;
    private String actionPerformed;
    private String referenceNumber;
}


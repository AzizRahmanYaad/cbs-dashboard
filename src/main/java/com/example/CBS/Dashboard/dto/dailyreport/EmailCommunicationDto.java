package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCommunicationDto {
    private Long id;
    private Boolean isInternal;
    private String sender;
    private String receiver;
    private String subject;
    private String summary;
    private String actionTaken;
    private Boolean followUpRequired;
}


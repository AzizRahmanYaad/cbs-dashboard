package com.example.CBS.Dashboard.dto.dailyreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingActivityDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private BigDecimal amount;
    private Boolean followUpRequired;
    private String responsiblePerson;
}


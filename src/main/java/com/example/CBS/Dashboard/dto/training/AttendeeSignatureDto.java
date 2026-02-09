package com.example.CBS.Dashboard.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendeeSignatureDto {
    private Long participantId;
    private String fullName;
    /** Base64-encoded signature image (data:image/png;base64,... or raw base64). */
    private String signatureData;
}

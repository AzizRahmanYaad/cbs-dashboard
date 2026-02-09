package com.example.CBS.Dashboard.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveSignatureRequest {
    /** Base64-encoded signature (data:image/png;base64,... or raw base64). */
    @NotBlank(message = "Signature data is required")
    private String signatureData;
}

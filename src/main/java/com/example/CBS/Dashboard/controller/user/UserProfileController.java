package com.example.CBS.Dashboard.controller.user;

import com.example.CBS.Dashboard.dto.user.SaveSignatureRequest;
import com.example.CBS.Dashboard.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @PutMapping("/signature")
    public ResponseEntity<Map<String, Boolean>> saveSignature(
            @Valid @RequestBody SaveSignatureRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        userService.saveSignature(username, request.getSignatureData());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/signature")
    public ResponseEntity<Map<String, Object>> getSignatureStatus(Authentication authentication) {
        String username = authentication.getName();
        String signatureData = userService.getSignatureData(username);
        return ResponseEntity.ok(Map.of(
                "hasSignature", signatureData != null && !signatureData.isBlank(),
                "signatureData", signatureData != null ? signatureData : ""
        ));
    }
}

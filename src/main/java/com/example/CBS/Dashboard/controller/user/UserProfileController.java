package com.example.CBS.Dashboard.controller.user;

import com.example.CBS.Dashboard.dto.user.ChangePasswordRequest;
import com.example.CBS.Dashboard.dto.user.SaveSignatureRequest;
import com.example.CBS.Dashboard.dto.user.UpdateProfileRequest;
import com.example.CBS.Dashboard.dto.user.UserDto;
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

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        userService.updateProfile(username, request.getFullName());
        UserDto updated = userService.getUserProfile(username);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

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

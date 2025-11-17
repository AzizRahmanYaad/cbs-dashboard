package com.example.CBS.Dashboard.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    private Boolean enabled;
}


package com.example.CBS.Dashboard.controller.admin;

import com.example.CBS.Dashboard.dto.user.CreateUserRequest;
import com.example.CBS.Dashboard.dto.user.RoleDto;
import com.example.CBS.Dashboard.dto.user.UpdateUserRequest;
import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.service.user.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> findAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> findUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> findAllRoles() {
        return ResponseEntity.ok(adminUserService.getAvailableRoles());
    }
}


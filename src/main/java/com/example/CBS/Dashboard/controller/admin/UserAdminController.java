package com.example.CBS.Dashboard.controller.admin;

import com.example.CBS.Dashboard.dto.user.CreateUserRequest;
import com.example.CBS.Dashboard.dto.user.ModuleRoleDto;
import com.example.CBS.Dashboard.dto.user.RoleDto;
import com.example.CBS.Dashboard.dto.user.UpdateUserRequest;
import com.example.CBS.Dashboard.dto.user.UserDto;
import com.example.CBS.Dashboard.service.user.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<Page<UserDto>> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminUserService.getUsers(pageable, search));
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<UserDto>> findAllUsersList() {
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

    @GetMapping("/roles/by-module")
    public ResponseEntity<List<ModuleRoleDto>> getRolesByModule() {
        return ResponseEntity.ok(adminUserService.getRolesByModule());
    }
}


package com.enterprise.wms.controller;

import com.enterprise.wms.domain.WmsEnums.RoleName;
import com.enterprise.wms.domain.entity.AppUser;
import com.enterprise.wms.dto.UserDtos.CreateUserRequest;
import com.enterprise.wms.dto.UserDtos.UpdateRolesRequest;
import com.enterprise.wms.dto.UserDtos.UserResponse;
import com.enterprise.wms.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public java.util.List<UserResponse> listUsers() {
        return userManagementService.listUsers().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        AppUser user = userManagementService.createUser(request.username(), request.password(), parseRoles(request.roles()));
        return toResponse(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @RequestBody UpdateRolesRequest request) {
        return toResponse(userManagementService.updateRoles(id, parseRoles(request.roles())));
    }

    private UserResponse toResponse(AppUser user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        return new UserResponse(user.getId(), user.getUsername(), roles);
    }

    private Set<RoleName> parseRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of(RoleName.WORKER);
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> RoleName.valueOf(role.trim().toUpperCase()))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}

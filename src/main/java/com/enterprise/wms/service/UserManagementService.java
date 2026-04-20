package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.RoleName;
import com.enterprise.wms.domain.entity.AppUser;
import com.enterprise.wms.domain.entity.Role;
import com.enterprise.wms.repository.AppUserRepository;
import com.enterprise.wms.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserManagementService {
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(AppUserRepository appUserRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> listUsers() {
        return appUserRepository.findAll();
    }

    @Transactional
    public AppUser createUser(String username, String rawPassword, Set<RoleName> roleNames) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (appUserRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        AppUser user = new AppUser();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(resolveRoles(roleNames));
        return appUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication == null ? "" : authentication.getName();
        if (user.getUsername() != null && user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        appUserRepository.delete(user);
    }

    @Transactional
    public AppUser updateRoles(Long userId, Set<RoleName> roleNames) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoles(resolveRoles(roleNames));
        return appUserRepository.save(user);
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<RoleName> requestedRoles = (roleNames == null || roleNames.isEmpty()) ? Set.of(RoleName.WORKER) : roleNames;
        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : requestedRoles) {
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role created = new Role();
                created.setName(roleName);
                return roleRepository.save(created);
            });
            roles.add(role);
        }
        return roles;
    }
}

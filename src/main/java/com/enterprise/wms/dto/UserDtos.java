package com.enterprise.wms.dto;

import java.util.Set;

public class UserDtos {
    public record CreateUserRequest(String username, String password, Set<String> roles) {}
    public record UpdateRolesRequest(Set<String> roles) {}
    public record UserResponse(Long id, String username, Set<String> roles) {}
}

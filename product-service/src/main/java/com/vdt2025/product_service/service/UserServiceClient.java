package com.vdt2025.product_service.service;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
class UserResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String phone;
    private String email;
    private String avatarName;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private RoleResponse role;
}
@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {
    @GetMapping("/{userId}")
    UserResponse getUser(@PathVariable("userId") String userId);
}

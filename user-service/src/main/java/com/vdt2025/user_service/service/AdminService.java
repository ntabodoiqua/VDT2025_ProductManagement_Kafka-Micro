package com.vdt2025.user_service.service;

import com.vdt2025.user_service.dto.request.user.UserFilterRequest;
import com.vdt2025.user_service.dto.request.user.UserUpdateRequest;
import com.vdt2025.user_service.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<UserResponse> getUsers(Pageable pageable);

    Page<UserResponse> searchUsers(UserFilterRequest filter, Pageable pageable);

    UserResponse getUserById(String id);

    UserResponse updateUser(String id, UserUpdateRequest request);

    UserResponse updateUserRole(String id, String roleName);

    String enableUser(String id);

    String disableUser(String id);

}

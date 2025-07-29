package com.vdt2025.user_service.service;

import com.vdt2025.common_dto.service.FileServiceClient;
import com.vdt2025.user_service.constant.PredefinedRole;
import com.vdt2025.user_service.dto.request.user.UserCreationRequest;
import com.vdt2025.user_service.dto.request.user.UserUpdateRequest;
import com.vdt2025.user_service.dto.response.UserResponse;
import com.vdt2025.user_service.entity.Role;
import com.vdt2025.user_service.entity.User;
import com.vdt2025.user_service.exception.AppException;
import com.vdt2025.user_service.exception.ErrorCode;
import com.vdt2025.user_service.mapper.UserMapper;
import com.vdt2025.user_service.repository.RoleRepository;
import com.vdt2025.user_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImp implements UserService{
    RoleRepository roleRepository;
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    FileServiceClient fileServiceClient;
    // FileStorageService fileStorageService;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Kiểm tra xem vai trò đã được định nghĩa chưa, nếu chưa thì tạo mới
        if (roleRepository.findById(PredefinedRole.GUEST_ROLE).isEmpty()) {
            Role guestRole = new Role();
            guestRole.setName(PredefinedRole.GUEST_ROLE);
            guestRole.setDescription("Guest role to be assigned to new users. Can view products and categories.");
            roleRepository.save(guestRole);
        }
        // Gán vai trò cho người dùng
        user.setRole(roleRepository.findById(PredefinedRole.GUEST_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)));
        // Gán trạng thái kích hoạt cho người dùng
        user.setEnabled(true);
        // Lưu người dùng vào cơ sở dữ liệu
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = "userCache",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public UserResponse getMyInfo() {
        log.info("Fetching current user information");
        // Lấy thông tin người dùng hiện tại từ SecurityContext
        var context = SecurityContextHolder.getContext();
        // Lấy tên người dùng từ Authentication
        String username = context.getAuthentication().getName();
        // Tìm người dùng trong cơ sở dữ liệu
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = "userCache", key = "#username")
    public UserResponse getUserByUsername(String username) {
        log.info("Fetching user information for username: {}", username);
        // Tìm người dùng theo tên đăng nhập
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'MANAGER')")
    public String changeMyPassword(String oldPassword, String newPassword) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        // Kiểm tra mật khẩu mới phải khác mật khẩu cũ
        if (oldPassword.equals(newPassword)) {
            throw new AppException(ErrorCode.OLD_PASSWORD_SAME_AS_NEW);
        }
        // Mã hóa mật khẩu mới và lưu vào cơ sở dữ liệu
        user.setPassword(passwordEncoder.encode(newPassword));
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION);
        }
        log.info("User {} changed password successfully", username);
        return "Password changed successfully";
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'MANAGER')")
    public String setMyAvatar(MultipartFile file) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        String fileName = fileServiceClient.uploadFile(file).getResult();
        user.setAvatarName(fileName);
        userRepository.save(user);
        return fileName;
    }


    @Override
    @CacheEvict(value = "userCache",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    @PreAuthorize("hasAnyRole('ADMIN', 'GUEST', 'MANAGER')")
    public UserResponse updateMyInfo(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Cập nhật thông tin người dùng từ request
        // Với các trường hợp không có trong request, sẽ giữ nguyên giá trị cũ
        // Người dùng không tự cập nhật role, chỉ có admin mới có thể cập nhật vai trò
        userMapper.updateUser(user, request);
        // Lưu người dùng đã cập nhật vào cơ sở dữ liệu

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION);
        }
        log.info("User {} updated their information successfully", username);
        return userMapper.toUserResponse(user);
    }

    @Override
    public String disableMyAccount() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Đặt trạng thái người dùng là không hoạt động
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User {} disabled their account successfully", username);
        return "Account disabled successfully";
    }

}

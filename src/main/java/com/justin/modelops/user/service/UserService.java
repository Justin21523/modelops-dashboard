package com.justin.modelops.user.service;

import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.security.RefreshTokenService;
import com.justin.modelops.user.dto.ChangePasswordRequest;
import com.justin.modelops.user.dto.UpdateProfileRequest;
import com.justin.modelops.user.dto.UserResponse;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.mapper.UserMapper;
import com.justin.modelops.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return userMapper.toResponse(getEntity(username));
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = getEntity(username);
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        auditService.record(AuditAction.UPDATE, "User", user.getId());
        return userMapper.toResponse(user);
    }

    /**
     * Changes the password after verifying the current one, then revokes all refresh
     * tokens so existing sessions cannot be silently continued.
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = getEntity(username);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.revokeAllForUser(user);
        auditService.record(AuditAction.PASSWORD_CHANGE, "User", user.getId());
    }

    private User getEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}

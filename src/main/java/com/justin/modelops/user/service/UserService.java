package com.justin.modelops.user.service;

import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.user.dto.UserResponse;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.mapper.UserMapper;
import com.justin.modelops.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return userMapper.toResponse(user);
    }
}

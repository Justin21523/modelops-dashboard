package com.justin.modelops.security;

import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private final JwtProperties jwtProperties =
            new JwtProperties("secret", Duration.ofHours(1), Duration.ofDays(30), "modelops-test");

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, jwtProperties);
    }

    private User user() {
        User user = new User();
        user.setUsername("alice");
        return user;
    }

    @Test
    void issue_returnsRawTokenAndStoresHash() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshTokenService.IssuedToken issued = service.issue(user());

        assertThat(issued.rawToken()).isNotBlank();
        assertThat(issued.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void rotate_revokesOldAndIssuesNew() {
        RefreshToken existing = new RefreshToken();
        existing.setUser(user());
        existing.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(existing));
        when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshTokenService.RotationResult result = service.rotate("some-raw-token");

        assertThat(existing.getRevokedAt()).isNotNull();
        assertThat(result.token().rawToken()).isNotBlank();
        assertThat(result.user().getUsername()).isEqualTo("alice");
    }

    @Test
    void rotate_rejectsRevokedToken() {
        RefreshToken existing = new RefreshToken();
        existing.setUser(user());
        existing.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        existing.setRevokedAt(Instant.now());
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.rotate("some-raw-token"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void rotate_rejectsUnknownToken() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate("nope"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
    }
}

package com.justin.modelops.security;

import com.justin.modelops.config.JpaAuditingConfig;
import com.justin.modelops.support.AbstractPostgresContainerTest;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies refresh-token persistence and bulk revocation against a real PostgreSQL instance.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class RefreshTokenRepositoryIT extends AbstractPostgresContainerTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsLookupAndRevokesAllForUser() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash");
        userRepository.saveAndFlush(user);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash("hash-abc");
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        refreshTokenRepository.saveAndFlush(token);

        assertThat(refreshTokenRepository.findByTokenHash("hash-abc")).isPresent();

        int revoked = refreshTokenRepository.revokeAllForUser(user, Instant.now());
        assertThat(revoked).isEqualTo(1);
    }
}

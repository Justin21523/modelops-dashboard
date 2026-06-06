package com.justin.modelops.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT settings bound from {@code app.security.jwt.*}.
 *
 * @param secret                 Base64-encoded HS256 signing secret
 * @param accessTokenExpiration  lifetime of issued access tokens
 * @param issuer                 token issuer claim
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, Duration accessTokenExpiration, String issuer) {
}

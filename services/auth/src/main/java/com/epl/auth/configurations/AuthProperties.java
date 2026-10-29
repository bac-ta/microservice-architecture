package com.epl.auth.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Settings under {@code epl.auth} (see application.yml).
 *
 * @param token          JWT issuing and login lockout
 * @param bootstrapAdmin first user, created at start-up while iam.app_user is empty
 */
@ConfigurationProperties("epl.auth")
public record AuthProperties(@DefaultValue Token token, @DefaultValue BootstrapAdmin bootstrapAdmin) {

    /**
     * Access / refresh token lifetimes are per client (iam.oauth_client).
     *
     * @param issuer          {@code iss} claim; the gateway checks it when its JWT_ISSUER is set
     * @param audience        {@code aud} claim
     * @param signingKey      EC P-256 private key as a JWK (JSON), from the secret store;
     *                        empty = a new key on every start (local development, single instance only)
     * @param retiredKeyGrace how long a replaced key stays in the JWKS (tokens it signed are still valid)
     * @param maxFailedLogins wrong passwords in a row before the account is locked for {@code lockDuration}
     */
    public record Token(
            @DefaultValue("epl-auth") String issuer,
            @DefaultValue("epl") String audience,
            String signingKey,
            @DefaultValue("24h") Duration retiredKeyGrace,
            @DefaultValue("5") int maxFailedLogins,
            @DefaultValue("15m") Duration lockDuration) {
    }

    /**
     * @param password empty = no user is created; the user must change it at first login
     */
    public record BootstrapAdmin(@DefaultValue("admin") String username, String password) {
    }
}

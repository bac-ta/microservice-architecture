package com.epl.auth;

import com.epl.auth.v1.AuthServiceGrpc;
import com.epl.auth.v1.ChangePasswordRequest;
import com.epl.auth.v1.CreateUserRequest;
import com.epl.auth.v1.LoginRequest;
import com.epl.auth.v1.LogoutRequest;
import com.epl.auth.v1.RefreshRequest;
import com.epl.auth.v1.TokenResponse;
import com.epl.auth.v1.User;
import com.epl.auth.v1.UserServiceGrpc;
import com.epl.auth.v1.UserType;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import com.epl.service.core.test.GrpcTestServerConfiguration;
import com.google.protobuf.Empty;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.epl.auth.TenantRegistryTests.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(GrpcTestServerConfiguration.class)
class AuthenticationTests {

    private static final String PASSWORD = "correct horse battery";

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "iam");
    }

    @Autowired
    private GrpcTestServer grpc;

    private AuthServiceGrpc.AuthServiceBlockingStub auth;
    private UserServiceGrpc.UserServiceBlockingStub users;

    @BeforeEach
    void stubs() {
        auth = AuthServiceGrpc.newBlockingStub(grpc.channel(null, null, null));
        users = UserServiceGrpc.newBlockingStub(grpc.channel(null, null, null));
    }

    @Test
    void loginIssuesATokenVerifiableWithTheJwks() throws Exception {
        var user = createUser("login.user");

        var tokens = login("LOGIN.USER", PASSWORD);
        assertThat(tokens.getTokenType()).isEqualTo("Bearer");
        assertThat(tokens.getExpiresInSeconds()).isEqualTo(15 * 60);
        assertThat(tokens.getMustChangePassword()).isTrue();

        var jwt = SignedJWT.parse(tokens.getAccessToken());
        var jwks = JWKSet.parse(auth.getJwks(Empty.getDefaultInstance()).getJwksJson());
        var key = jwks.getKeyByKeyId(jwt.getHeader().getKeyID()).toECKey();
        assertThat(jwt.verify(new ECDSAVerifier(key))).isTrue();
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo(user.getId());
        assertThat(jwt.getJWTClaimsSet().getStringClaim("usr")).isEqualTo("login.user");
        assertThat(jwt.getJWTClaimsSet().getStringClaim("typ")).isEqualTo("INTERNAL");
    }

    @Test
    void wrongPasswordsLockTheAccount() {
        createUser("lock.user");
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> login("lock.user", "wrong password"))
                    .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
        }
        assertThatThrownBy(() -> login("lock.user", PASSWORD))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
        assertThatThrownBy(() -> login("nobody", PASSWORD))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
    }

    @Test
    void refreshRotatesAndReuseEndsTheSession() {
        createUser("refresh.user");
        var first = login("refresh.user", PASSWORD);

        var second = refresh(first.getRefreshToken());
        assertThat(second.getRefreshToken()).isNotEqualTo(first.getRefreshToken());

        // the rotated token comes back: the whole family is revoked
        assertThatThrownBy(() -> refresh(first.getRefreshToken()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
        assertThatThrownBy(() -> refresh(second.getRefreshToken()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
    }

    @Test
    void logoutAndPasswordChangeEndSessions() {
        var user = createUser("logout.user");
        var session = login("logout.user", PASSWORD);
        auth.logout(LogoutRequest.newBuilder().setRefreshToken(session.getRefreshToken()).build());
        assertThatThrownBy(() -> refresh(session.getRefreshToken()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));

        var other = login("logout.user", PASSWORD);
        users.changePassword(ChangePasswordRequest.newBuilder().setUserId(user.getId())
                .setCurrentPassword(PASSWORD).setNewPassword("another long password").build());
        assertThatThrownBy(() -> refresh(other.getRefreshToken()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
        assertThat(login("logout.user", "another long password").getMustChangePassword()).isFalse();
    }

    @Test
    void unknownClientIsRejected() {
        createUser("client.user");
        assertThatThrownBy(() -> auth.login(LoginRequest.newBuilder()
                .setUsername("client.user").setPassword(PASSWORD).setClientId("NOPE").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.UNAUTHENTICATED));
    }

    private User createUser(String username) {
        return users.createUser(CreateUserRequest.newBuilder()
                .setUser(User.newBuilder().setUsername(username).setDisplayName(username).setType(UserType.USER_TYPE_INTERNAL))
                .setInitialPassword(PASSWORD).build());
    }

    private TokenResponse login(String username, String password) {
        return auth.login(LoginRequest.newBuilder().setUsername(username).setPassword(password).setClientId("PC_WEB").build());
    }

    private TokenResponse refresh(String refreshToken) {
        return auth.refresh(RefreshRequest.newBuilder().setRefreshToken(refreshToken).setClientId("PC_WEB").build());
    }
}

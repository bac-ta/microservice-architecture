package com.example.gateway;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "epl.gateway.security.issuer=https://auth.epl.test")
@AutoConfigureMockMvc
@Import(FakeBackends.class)
class SecurityGatewayTests {

    private static final String GET_PRODUCT = "/rpc/epl.masterdata.v1.ProductService/GetProduct";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakeBackends backends;

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mvc.perform(post(GET_PRODUCT).contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"P001\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenIsAcceptedAndForwarded() throws Exception {
        var token = token(FakeBackends.SIGNING_KEY, "https://auth.epl.test", Instant.now().plusSeconds(300));

        mvc.perform(post(GET_PRODUCT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"P001\"}"))
                .andExpect(status().isOk());

        var forwarded = backends.lastHeaders.get().get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        assertThat(forwarded).isEqualTo("Bearer " + token);
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        var token = token(FakeBackends.SIGNING_KEY, "https://auth.epl.test", Instant.now().minusSeconds(600));

        mvc.perform(post(GET_PRODUCT).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() throws Exception {
        var token = token(FakeBackends.SIGNING_KEY, "https://evil.test", Instant.now().plusSeconds(300));

        mvc.perform(post(GET_PRODUCT).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        var otherKey = new ECKeyGenerator(Curve.P_256).keyID("test-key").generate();
        var token = token(otherKey, "https://auth.epl.test", Instant.now().plusSeconds(300));

        mvc.perform(post(GET_PRODUCT).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicPathsNeedNoToken() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        // reaches the backend: the fake auth service does not implement Login
        mvc.perform(post("/rpc/epl.auth.v1.AuthService/Login")).andExpect(status().isNotImplemented());
    }

    private static String token(com.nimbusds.jose.jwk.ECKey key, String issuer, Instant expiresAt) throws JOSEException {
        var claims = new JWTClaimsSet.Builder()
                .subject("user-1")
                .issuer(issuer)
                .issueTime(Date.from(expiresAt.minusSeconds(900)))
                .expirationTime(Date.from(expiresAt))
                .build();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(key.getKeyID()).build(), claims);
        jwt.sign(new ECDSASigner(key));
        return jwt.serialize();
    }
}

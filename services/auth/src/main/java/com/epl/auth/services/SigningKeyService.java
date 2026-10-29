package com.epl.auth.services;

import com.epl.auth.configurations.AuthProperties;
import com.epl.auth.entities.SigningKeyEntity;
import com.epl.auth.repositories.SigningKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;

/**
 * ES256 key the access tokens are signed with, and the JWKS (AuthService/GetJwks) the gateway verifies them with.
 * The private key comes from {@code epl.auth.token.signing-key}; its public half is registered in iam.signing_key
 * as the ACTIVE key on first use (the previous ACTIVE key is RETIRED and stays published for a grace period).
 */
@RequiredArgsConstructor
@Service
public class SigningKeyService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SigningKeyService.class);
    private static final String CONFIGURED_KEY_REF = "config:epl.auth.token.signing-key";
    private static final String EPHEMERAL_KEY_REF = "ephemeral";

    private final SigningKeyRepository repository;
    private final TransactionTemplate transaction;
    private final AuthProperties authProperties;
    private ECKey key;
    private String privateKeyRef;
    private volatile boolean registered;

    @PostConstruct
    void loadKey() {
        var configured = authProperties.token().signingKey();
        if (configured == null || configured.isBlank()) {
            logger.warn("epl.auth.token.signing-key is not set: signing with a new key on every start (local development only)");
            key = generate();
            privateKeyRef = EPHEMERAL_KEY_REF;
        } else {
            key = parse(configured);
            privateKeyRef = CONFIGURED_KEY_REF;
        }
    }

    public String sign(JWTClaimsSet claims) {
        register();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(key.getKeyID())
                .type(JOSEObjectType.JWT)
                .build(), claims);
        try {
            jwt.sign(new ECDSASigner(key));
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign the access token", e);
        }
        return jwt.serialize();
    }

    /** RFC 7517 JSON Web Key Set with the public keys of every published key. */
    public String jwksJson() {
        register();
        var keys = new ArrayList<JWK>();
        for (var entity : repository.findPublished(Instant.now().minus(authProperties.token().retiredKeyGrace()))) {
            try {
                keys.add(JWK.parse(entity.getPublicJwk().toString()));
            } catch (ParseException e) {
                logger.error("Signing key {} has an invalid public_jwk; left out of the JWKS", entity.getKid(), e);
            }
        }
        return new JWKSet(keys).toString(true);
    }

    private void register() {
        if (registered) {
            return;
        }
        synchronized (this) {
            if (!registered) {
                transaction.executeWithoutResult(status -> activate());
                registered = true;
            }
        }
    }

    private void activate() {
        var now = Instant.now();
        var existing = repository.findById(key.getKeyID());
        if (existing.isPresent() && "ACTIVE".equals(existing.get().getStatus())) {
            return;
        }
        repository.findByStatus("ACTIVE").ifPresent(active -> {
            active.setStatus("RETIRED");
            active.setRetiredAt(now);
            // only one ACTIVE row may exist (ux_signing_key_one_active)
            repository.flush();
        });
        var entity = existing.orElseGet(() -> {
            var created = new SigningKeyEntity();
            created.setKid(key.getKeyID());
            created.setAlgorithm(JWSAlgorithm.ES256.getName());
            created.setPublicJwk(JSON.valueToTree(key.toPublicJWK().toJSONObject()));
            created.setPrivateKeyRef(privateKeyRef);
            created.setCreatedAt(now);
            return created;
        });
        entity.setStatus("ACTIVE");
        entity.setActivatedAt(now);
        entity.setRetiredAt(null);
        repository.save(entity);
        logger.info("Signing key {} is ACTIVE", key.getKeyID());
    }

    private static ECKey generate() {
        try {
            return new ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.ES256)
                    .keyIDFromThumbprint(true)
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot generate a signing key", e);
        }
    }

    private static ECKey parse(String json) {
        ECKey parsed;
        try {
            parsed = ECKey.parse(json);
        } catch (ParseException e) {
            throw new IllegalStateException("epl.auth.token.signing-key is not an EC JWK", e);
        }
        if (!Curve.P_256.equals(parsed.getCurve()) || !parsed.isPrivate()) {
            throw new IllegalStateException("epl.auth.token.signing-key must be a private P-256 key (ES256)");
        }
        if (parsed.getKeyID() != null) {
            return parsed;
        }
        try {
            return new ECKey.Builder(parsed).keyIDFromThumbprint().build();
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot compute the key id of epl.auth.token.signing-key", e);
        }
    }
}

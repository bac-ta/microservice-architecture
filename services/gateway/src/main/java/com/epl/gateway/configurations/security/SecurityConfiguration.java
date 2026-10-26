package com.epl.gateway.configurations.security;

import com.epl.auth.v1.AuthServiceGrpc;
import com.epl.gateway.configurations.GatewayProperties;
import com.epl.gateway.rpc.RpcInvoker;
import com.epl.gateway.rpc.RpcRegistry;
import com.google.protobuf.Empty;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.grpc.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Every request needs a JWT issued by the auth service, except {@code epl.gateway.security.public-paths}.
 * The gateway rejects bad tokens early; backends verify the token again and enforce tenant grants / ABAC.
 */
@Configuration
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
    private static final long JWKS_REFRESH_TIMEOUT_MS = 5_000;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayProperties properties) throws Exception {
        var security = properties.security();
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        if (!security.enabled()) {
            logger.warn("epl.gateway.security.enabled=false: requests are NOT authenticated");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(security.publicPaths().toArray(String[]::new)).permitAll()
                        // error dispatches keep the status of the original failure instead of turning into 401
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "epl.gateway.security", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder jwtDecoder(GatewayProperties properties, RpcInvoker invoker) {
        var security = properties.security();
        var authBackend = RpcRegistry.backendOf(AuthServiceGrpc.SERVICE_NAME);
        var jwkSource = JWKSourceBuilder.create(new GrpcJwkSetSource(() -> invoker
                        .unary(authBackend, AuthServiceGrpc.getGetJwksMethod(), Empty.getDefaultInstance(), new Metadata())
                        .getJwksJson()))
                .cache(security.jwksCacheTtl().toMillis(), JWKS_REFRESH_TIMEOUT_MS)
                .build();

        var algorithms = security.jwsAlgorithms().stream().map(JWSAlgorithm::parse).collect(Collectors.toSet());
        var processor = new DefaultJWTProcessor<SecurityContext>();
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(algorithms, jwkSource));
        // claims (exp, nbf, iss) are checked by the Spring validators below
        processor.setJWTClaimsSetVerifier((claims, context) -> {
        });

        var decoder = new NimbusJwtDecoder(processor);
        var issuer = security.issuer();
        decoder.setJwtValidator(issuer == null || issuer.isBlank()
                ? JwtValidators.createDefault()
                : JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(GatewayProperties properties) {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(properties.cors().allowedOrigins().stream().filter(origin -> !origin.isBlank()).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        cors.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT_LANGUAGE,
                "X-Company", "X-Warehouse", "X-Customer", "X-Request-Id"));
        cors.setExposedHeaders(List.of("X-Request-Id", HttpHeaders.CONTENT_DISPOSITION));
        cors.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}

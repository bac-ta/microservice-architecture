package com.entropy.gateway.configurations.securities.jwt;

import com.entropy.backend.common.constant.ExceptionMessage;
import com.entropy.backend.pattern.factory.JwtTokenProviderFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bac-ta
 */
@Component("jwtTokenProviderImpl")
public class JwtTokenProviderImpl implements JwtTokenProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderImpl.class);
    @Value("${app.auth.token-secret}")
    private String clientSecrectKey;
    @Value("${app.auth.token-expiration-msec}")
    private int expirationInMs;

    @Override
    public String generateToken(Authentication authentication) {
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        Date dateNow = new Date();
        Date expiryDate = new Date(dateNow.getTime() + expirationInMs);

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("username", principal.getUsername());
        claimMap.put("email", principal.getEmail());
        claimMap.put("authorities", principal.getAuthorities());
        claimMap.put("id", principal.getId());

        return Jwts.builder().setId(Long.toString(principal.getId())).setClaims(claimMap).setIssuedAt(dateNow).
                setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512, clientSecrectKey).compact();
    }

    @Override
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser().setSigningKey(clientSecrectKey).parseClaimsJws(token).getBody();
        return Long.valueOf(claims.get("id").toString());
    }

    @Override
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(clientSecrectKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error(ExceptionMessage.INVALID_JWT_SIGNATURE);
        } catch (MalformedJwtException | IllegalArgumentException ex) {
            logger.error(ExceptionMessage.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException ex) {
            logger.error(ExceptionMessage.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException ex) {
            logger.error(ExceptionMessage.UNSUPPORT_JWT_TOKEN);
        }
        return false;
    }
}

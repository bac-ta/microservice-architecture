package com.entropy.gateway.configurations.securities.jwts;

import com.entropy.backend.common.constants.ExceptionMessage;
import com.entropy.backend.models.entities.Permission;
import com.entropy.backend.models.entities.Role;
import com.entropy.backend.models.entities.User;
import com.entropy.backend.models.enumerations.StatusType;
import com.entropy.backend.models.exceptions.AccountNotFoundException;
import com.entropy.backend.patterns.factories.JwtTokenProviderFactory;
import com.entropy.backend.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class handle JWT (generate, validate...) implement {@link JwtTokenProviderFactory}
 *
 * @author bac-ta
 * @see JwtTokenProviderImpl
 * @since 2021-05-31
 */
@Component("jwtTokenProviderImpl")
public class JwtTokenProviderImpl implements JwtTokenProviderFactory {
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderImpl.class);
    @Value("${app.auth.token-secret}")
    private String clientSecrectKey;
    @Value("${app.auth.token-expiration-msec}")
    private int expirationInMs;

    @Autowired
    public JwtTokenProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String generateToken(Authentication authentication) {
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        Date dateNow = new Date();
        Date expiryDate = new Date(dateNow.getTime() + expirationInMs);

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("username", principal.getUsername());
        claimMap.put("email", principal.getEmail());
        claimMap.put("authorities", principal.getAuthorities());

        return Jwts.builder().setId(principal.getUsername()).setClaims(claimMap).setIssuedAt(dateNow).
                setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512, clientSecrectKey).compact();
    }

    @Override
    public String generateToken(String emailOrUsername) {
        Date dateNow = new Date();
        Date expiryDate = new Date(dateNow.getTime() + expirationInMs);

        Optional<User> optionalUser = userRepository.loadUserAndAuthorities(emailOrUsername, (byte) StatusType.ON.getValue());
        if (!optionalUser.isPresent())
            throw new AccountNotFoundException(emailOrUsername);

        User user = optionalUser.get();
        Set<Role> roles = user.getRoles();
        Set<Permission> permissions = roles.stream().
                flatMap(role -> role.getPermissions().stream()).
                collect(Collectors.toSet());

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("username", user.getUsername());
        claimMap.put("email", user.getEmail());

        Set<GrantedAuthority> authorities = new HashSet<>();
        roles.forEach(role ->
                authorities.add(new SimpleGrantedAuthority(role.getName().name()))
        );

        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName().name())));
        claimMap.put("authorities", authorities);

        return Jwts.builder().setId(user.getUsername()).setClaims(claimMap).setIssuedAt(dateNow).
                setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512, clientSecrectKey).compact();
    }

    @Override
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser().setSigningKey(clientSecrectKey).parseClaimsJws(token).getBody();
        return claims.get("id").toString();
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

package com.entropy.gateway.configurations.securities.auditing;

import com.entropy.gateway.configurations.securities.jwts.AccountPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Class config auditing which traces "who called login API (administrator or user or anonymous user)
 *
 * @author bac-ta
 * @see AuditingConfiguration
 * @since 2021-05-31
 */
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Configuration
public class AuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditAwareImpl();
    }
}

class SpringSecurityAuditAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken)
            return Optional.of("Anonymous user");


        AccountPrincipal accountPrincipal = (AccountPrincipal) authentication.getPrincipal();

        return Optional.ofNullable(accountPrincipal.getUsername());
    }
}
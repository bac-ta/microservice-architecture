package com.entropy.gateway.configurations.securities.jwts;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class create account principal use by JWT
 *
 * @author bac-ta
 * @see AccountPrincipal
 * @since 2021-05-31
 */
@Data

public class AccountPrincipal implements UserDetails {
    private String username;
    private String email;
    private String phone;
    private String password;
    private String name; //For oauth2
    private Map<String, Object> attributes;//For oath2
    private Collection<? extends GrantedAuthority> authorities;

    public static AccountPrincipal create(String username, String email, String phone, Set<String> roleNames, Set<String> permissionNames) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        roleNames.forEach(roleName -> {
            authorities.add(new SimpleGrantedAuthority(roleName));
        });
        permissionNames.forEach(name -> authorities.add(new SimpleGrantedAuthority(name)));

        AccountPrincipal principal = new AccountPrincipal();
        principal.setEmail(email);
        principal.setUsername(username);
        principal.setPhone(phone);
        principal.setAuthorities(authorities);

        return principal;
    }


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}

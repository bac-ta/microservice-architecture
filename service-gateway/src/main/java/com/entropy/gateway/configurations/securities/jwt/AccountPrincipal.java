package com.entropy.gateway.configurations.securities.jwt;

import com.entropy.backend.model.entity.User;
import com.entropy.backend.model.enumeration.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author bac-ta
 */
@Data
@NoArgsConstructor
public class AccountPrincipal implements UserDetails, OAuth2User {
    private Long id;
    private String email;
    private String userName;
    private String password;
    private String name; //For oauth2
    private Map<String, Object> attributes;//For oath2
    private Collection<? extends GrantedAuthority> authorities;

    public static AccountPrincipal create(User user) {
        String userType = user.getUserType().name();
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(userType));
        AccountPrincipal principal = new AccountPrincipal();

        principal.setId(user.getId());
        principal.setEmail(user.getEmail());
        principal.setUserName(user.getUserName());
        principal.setPassword(user.getPassword());
        principal.setAuthorities(authorities);

        return principal;
    }

    public static AccountPrincipal create(com.entropy.backend.model.entity.OAuth2User oAuth2UserEntity, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = Collections.
                singletonList(new SimpleGrantedAuthority(UserType.NORMAL.name()));

        AccountPrincipal principal = new AccountPrincipal();
        principal.setAuthorities(authorities);
        principal.setEmail(oAuth2UserEntity.getEmail());
        principal.setName(oAuth2UserEntity.getName());
        principal.setAttributes(attributes);

        return principal;
    }

    @Override
    public String getUsername() {
        return userName;
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

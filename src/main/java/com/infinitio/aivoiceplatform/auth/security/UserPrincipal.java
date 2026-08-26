package com.infinitio.aivoiceplatform.auth.security;

import com.infinitio.aivoiceplatform.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User Principal.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
public class UserPrincipal
        implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required."
            );
        }

        this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {

        if (user.getRole() == null
                || user.getRole().getRoleCode() == null
                || user.getRole().getRoleCode().isBlank()) {

            return List.of();
        }

        return List.of(
                new SimpleGrantedAuthority(
                        user.getRole()
                                .getRoleCode()
                )
        );
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }


    @Override
    public String getUsername() {
        return user.getEmail();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    @Override
    public boolean isAccountNonLocked() {

        if (!Boolean.TRUE.equals(
                user.getAccountLocked())) {

            return true;
        }

        LocalDateTime lockedUntil =
                user.getAccountLockedUntil();

        /*
         * Permanent lock.
         */
        if (lockedUntil == null) {
            return false;
        }

        /*
         * Temporary lock has expired.
         */
        return lockedUntil.isBefore(
                LocalDateTime.now()
        );
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    @Override
    public boolean isEnabled() {

        return Integer.valueOf(1)
                .equals(user.getIsActive())
                && Integer.valueOf(0)
                .equals(user.getIsDeleted());
    }
}
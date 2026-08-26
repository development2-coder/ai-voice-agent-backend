package com.infinitio.aivoiceplatform.auth.security;

import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom User Details Service.
 *
 * Loads authenticated user and required authorization data
 * from database for Spring Security.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private static final Integer NOT_DELETED = 0;

    private final UserRepository userRepository;


    /**
     * Load user by username.
     *
     * Email is used as username.
     *
     * Role is fetched together with User because
     * UserPrincipal requires role information while
     * creating Spring Security authorities.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {

        log.info(
                "Loading authenticated user. email={}",
                email
        );

        User user =
                userRepository
                        .findByEmailAndIsDeleted(
                                email,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found with email: "
                                                + email
                                )
                        );

        /*
         * Force role initialization while the
         * Hibernate session is still active.
         *
         * EntityGraph already fetches the role,
         * but this also makes the requirement explicit.
         */
        if (user.getRole() != null) {

            user.getRole().getRoleCode();
        }

        log.debug(
                "Authenticated user loaded successfully. email={}, role={}",
                user.getEmail(),
                user.getRole() != null
                        ? user.getRole().getRoleCode()
                        : null
        );

        return new UserPrincipal(user);
    }
}
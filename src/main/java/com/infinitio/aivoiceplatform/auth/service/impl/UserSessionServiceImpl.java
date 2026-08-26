package com.infinitio.aivoiceplatform.auth.service.impl;

import com.infinitio.aivoiceplatform.auth.dto.response.UserSessionResponse;
import com.infinitio.aivoiceplatform.auth.entity.UserSession;
import com.infinitio.aivoiceplatform.auth.enums.DeviceType;
import com.infinitio.aivoiceplatform.auth.enums.LoginType;
import com.infinitio.aivoiceplatform.auth.mapper.UserSessionMapper;
import com.infinitio.aivoiceplatform.auth.repository.UserSessionRepository;
import com.infinitio.aivoiceplatform.auth.service.UserSessionService;
import com.infinitio.aivoiceplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Session Service Implementation.
 *
 * Handles user authentication session operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSessionServiceImpl
        implements UserSessionService {

    private static final Integer NOT_DELETED = 0;

    private final UserSessionRepository userSessionRepository;

    private final UserSessionMapper userSessionMapper;


    // =========================================================
    // CREATE SESSION
    // =========================================================

    @Override
    public UserSession createSession(
            User user,
            LoginType loginType,
            DeviceType deviceType,
            String ipAddress,
            String userAgent) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required to create a session."
            );
        }

        if (loginType == null) {
            throw new IllegalArgumentException(
                    "Login type is required."
            );
        }

        if (deviceType == null) {
            throw new IllegalArgumentException(
                    "Device type is required."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        UserSession session =
                UserSession.builder()
                        .user(user)
                        .loginType(loginType)
                        .deviceType(deviceType)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .loginTime(now)
                        .lastActivity(now)
                        .active(true)
                        .build();

        // Set audit field BEFORE saving
        session.setCreatedBy(user.getId());

        UserSession savedSession =
                userSessionRepository.save(session);

        log.info(
                "User session created. sessionPublicId={}",
                savedSession.getPublicId()
        );

        return savedSession;
    }

    // =========================================================
    // UPDATE LAST ACTIVITY
    // =========================================================

    @Override
    public void updateLastActivity(
            UserSession session) {

        if (session == null) {
            return;
        }

        if (!Boolean.TRUE.equals(
                session.getActive())) {

            return;
        }

        if (!NOT_DELETED.equals(
                session.getIsDeleted())) {

            return;
        }

        session.setLastActivity(
                LocalDateTime.now()
        );

        userSessionRepository.save(
                session
        );
    }


    // =========================================================
    // LOGOUT SESSION
    // =========================================================

    @Override
    public void logout(
            UserSession session) {

        if (session == null) {
            return;
        }

        if (!Boolean.TRUE.equals(
                session.getActive())) {

            return;
        }

        session.setActive(false);

        session.setLogoutTime(
                LocalDateTime.now()
        );

        userSessionRepository.save(
                session
        );

        log.info(
                "User session logged out. sessionPublicId={}",
                session.getPublicId()
        );
    }


    // =========================================================
    // LOGOUT ALL SESSIONS
    // =========================================================

    @Override
    public void logoutAll(
            User user) {

        if (user == null) {
            return;
        }

        List<UserSession> sessions =
                userSessionRepository
                        .findByUserAndActiveAndIsDeleted(
                                user,
                                true,
                                NOT_DELETED
                        );

        if (sessions.isEmpty()) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        sessions.forEach(session -> {

            session.setActive(false);

            session.setLogoutTime(now);

        });

        userSessionRepository.saveAll(
                sessions
        );

        log.info(
                "All active sessions logged out. userPublicId={}",
                user.getPublicId()
        );
    }


    // =========================================================
    // GET ACTIVE SESSIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getActiveSessions(
            User user) {

        if (user == null) {
            return List.of();
        }

        return userSessionRepository
                .findByUserAndActiveAndIsDeleted(
                        user,
                        true,
                        NOT_DELETED
                )
                .stream()
                .map(userSessionMapper::toResponse)
                .toList();
    }
}
package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.auth.dto.response.UserSessionResponse;
import com.infinitio.aivoiceplatform.auth.entity.UserSession;
import com.infinitio.aivoiceplatform.auth.enums.DeviceType;
import com.infinitio.aivoiceplatform.auth.enums.LoginType;
import com.infinitio.aivoiceplatform.user.entity.User;

import java.util.List;

/**
 * User Session Service.
 *
 * Handles user authentication session operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface UserSessionService {

    /**
     * Creates a user session.
     *
     * @param user user
     * @param loginType login type
     * @param deviceType device type
     * @param ipAddress client IP address
     * @param userAgent client user agent
     * @return created session
     */
    UserSession createSession(
            User user,
            LoginType loginType,
            DeviceType deviceType,
            String ipAddress,
            String userAgent
    );

    /**
     * Updates session activity.
     *
     * @param session session
     */
    void updateLastActivity(
            UserSession session
    );

    /**
     * Logs out a session.
     *
     * @param session session
     */
    void logout(
            UserSession session
    );

    /**
     * Logs out all sessions for a user.
     *
     * @param user user
     */
    void logoutAll(
            User user
    );

    /**
     * Gets active sessions.
     *
     * @param user user
     * @return active sessions
     */
    List<UserSessionResponse> getActiveSessions(
            User user
    );
}
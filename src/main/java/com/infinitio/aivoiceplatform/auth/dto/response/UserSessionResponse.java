package com.infinitio.aivoiceplatform.auth.dto.response;

import com.infinitio.aivoiceplatform.auth.enums.LoginType;
import com.infinitio.aivoiceplatform.auth.enums.DeviceType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User Session Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionResponse {

    private String sessionPublicId;

    private LoginType loginType;

    private DeviceType deviceType;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime loginTime;

    private LocalDateTime lastActivity;

    private Boolean active;

}
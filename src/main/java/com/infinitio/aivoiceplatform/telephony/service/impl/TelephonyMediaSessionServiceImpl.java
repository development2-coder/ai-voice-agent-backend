package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.telephony.constants.TelephonyConstants;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyMediaSessionService;
import com.infinitio.aivoiceplatform.voicegateway.websocket.VoiceGatewayWebSocketSessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Default implementation for telephony media-session control.
 *
 * <p>
 * The service uses the existing Voice Gateway WebSocket session
 * registry because provider WebSocket sessions are already
 * registered there during Exotel stream initialization.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelephonyMediaSessionServiceImpl
        implements TelephonyMediaSessionService {

    private final VoiceGatewayWebSocketSessionRegistry
            webSocketSessionRegistry;

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeSession(
            String providerCode,
            String callId,
            String reason) {

        if (providerCode == null
                || providerCode.isBlank()) {

            log.warn(
                    "Cannot close telephony media session because "
                            + "provider code is missing. callId={}",
                    callId
            );

            return;
        }

        if (callId == null
                || callId.isBlank()) {

            log.warn(
                    "Cannot close telephony media session because "
                            + "call ID is missing. provider={}",
                    providerCode
            );

            return;
        }

        if (!TelephonyConstants.PROVIDER_EXOTEL
                .equalsIgnoreCase(
                        providerCode.trim()
                )) {

            log.warn(
                    "Media session close is not implemented for "
                            + "provider. provider={}, callId={}",
                    providerCode,
                    callId
            );

            return;
        }

        log.info(
                "Closing telephony media session. "
                        + "provider={}, callId={}, reason={}",
                providerCode,
                callId,
                reason
        );

        webSocketSessionRegistry.close(
                callId
        );

        log.info(
                "Telephony media session close requested. "
                        + "provider={}, callId={}",
                providerCode,
                callId
        );
    }
}
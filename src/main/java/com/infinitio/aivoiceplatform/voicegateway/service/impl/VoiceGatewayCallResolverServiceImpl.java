package com.infinitio.aivoiceplatform.voicegateway.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayMessages;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayCallResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of
 * {@link VoiceGatewayCallResolverService}.
 *
 * <p>
 * Resolves the internal application Call public identifier
 * from the telephony provider call identifier.
 * </p>
 *
 * <p>
 * Provider identifiers and application identifiers are kept
 * separate intentionally. The provider call identifier is used
 * only to locate the corresponding application Call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoiceGatewayCallResolverServiceImpl
        implements VoiceGatewayCallResolverService {

    private final CallRepository callRepository;

    /**
     * Resolves the application Call public identifier from
     * the provider call identifier.
     *
     * @param providerCallId provider supplied call identifier
     * @return application Call public identifier
     */
    @Override
    public String resolveCallId(
            String providerCallId) {

        validateProviderCallId(
                providerCallId
        );

        log.debug(
                "Resolving application call. " +
                        "providerCallId={}",
                providerCallId
        );

        Call call =
                callRepository
                        .findByProviderCallId(
                                providerCallId
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "No application call found for " +
                                            "provider call. " +
                                            "providerCallId={}",
                                    providerCallId
                            );

                            return new IllegalArgumentException(
                                    VoiceGatewayMessages.CALL_ID_REQUIRED
                            );
                        });

        if (call.getPublicId() == null
                || call.getPublicId().isBlank()) {

            log.error(
                    "Resolved application call has no public ID. " +
                            "providerCallId={}, databaseId={}",
                    providerCallId,
                    call.getId()
            );

            throw new IllegalStateException(
                    VoiceGatewayMessages.RUNTIME_STATE_UNAVAILABLE
            );
        }

        log.info(
                "Application call resolved successfully. " +
                        "providerCallId={}, callPublicId={}",
                providerCallId,
                call.getPublicId()
        );

        return call.getPublicId();
    }

    /**
     * Validates the provider call identifier.
     *
     * @param providerCallId provider supplied call identifier
     */
    private void validateProviderCallId(
            String providerCallId) {

        if (providerCallId == null
                || providerCallId.isBlank()) {

            log.warn(
                    "Provider call identifier is missing."
            );

            throw new IllegalArgumentException(
                    VoiceGatewayMessages.CALL_ID_REQUIRED
            );
        }
    }
}
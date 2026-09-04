package com.infinitio.aivoiceplatform.voicegateway.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayMessages;
import com.infinitio.aivoiceplatform.voicegateway.service.VoiceGatewayCallResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

        log.info(
                "Resolving provider call. providerCallId=[{}], length={}, hex={}",
                providerCallId,
                providerCallId == null
                        ? null
                        : providerCallId.length(),
                providerCallId == null
                        ? null
                        : java.util.HexFormat.of().formatHex(
                        providerCallId.getBytes(
                                StandardCharsets.UTF_8
                        )
                )
        );

        validateProviderCallId(providerCallId);

        log.debug(
                "Searching application call using providerCallId={}",
                providerCallId
        );

        Optional<Call> callOptional =
                callRepository.findByProviderCallId(
                        providerCallId
                );

        if (callOptional.isEmpty()) {

            log.warn(
                    "JPQL provider call lookup returned no result. " +
                            "Trying native database lookup. " +
                            "providerCallId={}",
                    providerCallId
            );

            callOptional =
                    callRepository.findByProviderCallIdNative(
                            providerCallId
                    );
        }

        Call call =
                callOptional.orElseThrow(() -> {

                    log.error(
                            "Unable to resolve application call. " +
                                    "providerCallId={}, length={}, hex={}",
                            providerCallId,
                            providerCallId.length(),
                            java.util.HexFormat.of().formatHex(
                                    providerCallId.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );

                    return new IllegalArgumentException(
                            "No application call found for provider call ID: "
                                    + providerCallId
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
                        "providerCallId={}, databaseId={}, callPublicId={}",
                providerCallId,
                call.getId(),
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
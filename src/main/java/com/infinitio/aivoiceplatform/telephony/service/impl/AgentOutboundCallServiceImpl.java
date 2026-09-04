package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.phonenumber.repository.PhoneNumberRepository;
import com.infinitio.aivoiceplatform.telephony.config.TelephonyMediaProperties;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceAgentOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.AgentOutboundCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.AgentOutboundCallService;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Starts a direct outbound call for an Agent Flow.
 *
 * <p>
 * The service is intentionally provider-independent.
 * The telephony provider is resolved from the phone number
 * assigned to the Agent.
 * </p>
 *
 * <p>
 * This service does not contain provider-specific configuration
 * or provider-specific implementation details. Provider-specific
 * behavior is handled by the telephony provider adapter layer.
 * </p>
 *
 * <p>
 * Campaign and CampaignContact are deliberately not involved.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentOutboundCallServiceImpl
        implements AgentOutboundCallService {

    private static final Integer NOT_DELETED = 0;

    private static final Integer ACTIVE = 1;

    private static final String OUTBOUND = "OUTBOUND";

    private static final String INITIATED = "INITIATED";

    private final FlowRepository flowRepository;

    private final PhoneNumberRepository phoneNumberRepository;

    private final CallRepository callRepository;

    private final CallSessionCreateService
            callSessionCreateService;

    private final TelephonyService telephonyService;

    private final CurrentUserService currentUserService;

    private final TelephonyMediaProperties
            telephonyMediaProperties;

    /**
     * Places a direct outbound call for an Agent Flow.
     *
     * <p>
     * The provider is determined from the selected phone number.
     * This allows the same Agent outbound-call flow to work with
     * different CPaaS providers without changing the Agent layer.
     * </p>
     *
     * @param request agent outbound call request
     * @return outbound call response
     */
    @Override
    public AgentOutboundCallResponseDto
    placeAgentOutboundCall(
            PlaceAgentOutboundCallRequestDto request) {

        validateRequest(
                request
        );

        /*
         * ---------------------------------------------------------
         * FLOW
         * ---------------------------------------------------------
         */

        Flow flow =
                flowRepository
                        .findByPublicIdAndIsDeleted(
                                request.getFlowPublicId(),
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Flow not found."
                                )
                        );

        if (flow.getStatus()
                != FlowStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Flow must be ACTIVE before placing "
                            + "an outbound call."
            );
        }

        if (flow.getAgent() == null) {

            throw new IllegalStateException(
                    "Flow is not associated with an Agent."
            );
        }

        /*
         * ---------------------------------------------------------
         * PHONE NUMBER
         * ---------------------------------------------------------
         */

        PhoneNumber phoneNumber =
                phoneNumberRepository
                        .findByPublicId(
                                request.getPhoneNumberPublicId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Phone number not found."
                                )
                        );

        if (!ACTIVE.equals(
                phoneNumber.getIsActive()
        )) {

            throw new IllegalStateException(
                    "Phone number is inactive."
            );
        }

        if (!NOT_DELETED.equals(
                phoneNumber.getIsDeleted()
        )) {

            throw new IllegalStateException(
                    "Phone number is deleted."
            );
        }

        /*
         * ---------------------------------------------------------
         * PROVIDER
         * ---------------------------------------------------------
         *
         * Provider is taken from the selected phone number.
         *
         * Example:
         *
         * EXOTEL
         * OTHER_PROVIDER
         *
         * No provider is hardcoded here.
         */

        String providerCode =
                phoneNumber.getProvider();

        if (providerCode == null
                || providerCode.isBlank()) {

            throw new IllegalStateException(
                    "Phone number provider is not configured."
            );
        }

        providerCode =
                providerCode.trim();

        log.info(
                "Preparing Agent outbound call. "
                        + "flowPublicId={}, phoneNumberPublicId={}, "
                        + "provider={}",
                flow.getPublicId(),
                phoneNumber.getPublicId(),
                providerCode
        );

        /*
         * ---------------------------------------------------------
         * AGENT ↔ PHONE NUMBER
         * ---------------------------------------------------------
         */

        if (phoneNumber.getAgent() == null
                || !phoneNumber
                .getAgent()
                .getId()
                .equals(
                        flow.getAgent()
                                .getId()
                )) {

            throw new IllegalStateException(
                    "Phone number is not assigned to "
                            + "the Agent of this Flow."
            );
        }

        /*
         * ---------------------------------------------------------
         * CREATE CALL
         * ---------------------------------------------------------
         *
         * CampaignContact is intentionally NULL.
         *
         * Provider is stored dynamically from the selected
         * phone number.
         */

        Call call =
                Call.builder()
                        .campaignContact(null)
                        .provider(providerCode)
                        .fromNumber(
                                phoneNumber
                                        .getPhoneNumber()
                        )
                        .toNumber(
                                request.getToNumber()
                        )
                        .direction(OUTBOUND)
                        .status(INITIATED)
                        .startedAt(
                                LocalDateTime.now()
                        )
                        .description(
                                "Direct Agent outbound call."
                        )
                        .createdBy(
                                currentUserService
                                        .getCurrentUserId()
                        )
                        .build();

        Call savedCall =
                callRepository.save(
                        call
                );

        log.info(
                "Outbound Call created. "
                        + "callPublicId={}, provider={}",
                savedCall.getPublicId(),
                providerCode
        );

        /*
         * ---------------------------------------------------------
         * CREATE CALL SESSION
         * ---------------------------------------------------------
         */

        CreateCallSessionRequestDto
                callSessionRequest =
                CreateCallSessionRequestDto
                        .builder()
                        .callId(
                                savedCall
                                        .getPublicId()
                        )
                        .tenantId(
                                flow.getAgent()
                                        .getTenant()
                                        .getPublicId()
                        )
                        .agentId(
                                flow.getAgent()
                                        .getPublicId()
                        )
                        .agentVersion(
                                flow.getVersion()
                        )
                        .flowPublicId(
                                flow.getPublicId()
                        )
                        .language(
                                flow.getAgent()
                                        .getLanguage()
                        )
                        .build();

        callSessionCreateService
                .createCallSession(
                        callSessionRequest,
                        currentUserService
                                .getCurrentUserId()
                );

        /*
         * ---------------------------------------------------------
         * BUILD APPLICATION-OWNED STREAM URL
         * ---------------------------------------------------------
         */

        String streamUrl =
                buildStreamUrl(
                        savedCall.getPublicId()
                );

        /*
         * ---------------------------------------------------------
         * BUILD GENERIC PROVIDER REQUEST
         * ---------------------------------------------------------
         *
         * The Agent service only supplies generic telephony
         * information.
         *
         * Provider-specific mapping is handled inside the
         * selected TelephonyProvider implementation.
         */

        PlaceOutboundCallRequestDto
                providerRequest =
                PlaceOutboundCallRequestDto
                        .builder()
                        .callPublicId(
                                savedCall
                                        .getPublicId()
                        )
                        .fromNumber(
                                phoneNumber
                                        .getPhoneNumber()
                        )
                        .toNumber(
                                request.getToNumber()
                        )
                        .streamUrl(
                                streamUrl
                        )
                        .build();

        ProviderCallResponseDto
                providerResponse;

        try {

            providerResponse =
                    telephonyService
                            .placeOutboundCall(
                                    providerCode,
                                    providerRequest
                            );

        } catch (Exception exception) {

            log.error(
                    "Failed to place Agent outbound call. "
                            + "callPublicId={}, provider={}",
                    savedCall.getPublicId(),
                    providerCode,
                    exception
            );

            throw exception;
        }

        log.info(
                "Agent outbound call initiated successfully. "
                        + "callPublicId={}, providerCallId={}, "
                        + "provider={}, flowPublicId={}, "
                        + "agentPublicId={}",
                savedCall.getPublicId(),
                providerResponse.getProviderCallId(),
                providerCode,
                flow.getPublicId(),
                flow.getAgent()
                        .getPublicId()
        );

        return AgentOutboundCallResponseDto
                .builder()
                .callPublicId(
                        savedCall
                                .getPublicId()
                )
                .providerCallId(
                        providerResponse
                                .getProviderCallId()
                )
                .provider(
                        providerResponse
                                .getProvider()
                )
                .status(
                        providerResponse
                                .getStatus()
                )
                .flowPublicId(
                        flow.getPublicId()
                )
                .agentPublicId(
                        flow.getAgent()
                                .getPublicId()
                )
                .phoneNumberPublicId(
                        phoneNumber
                                .getPublicId()
                )
                .fromNumber(
                        phoneNumber
                                .getPhoneNumber()
                )
                .toNumber(
                        request.getToNumber()
                )
                .streamUrl(
                        streamUrl
                )
                .build();
    }

    /**
     * Builds the application-owned media streaming URL.
     *
     * <p>
     * The URL is configured independently of the CPaaS provider.
     * The call public ID is appended only for application-level
     * call correlation.
     * </p>
     *
     * @param callPublicId application call public ID
     * @return media streaming URL
     */
    private String buildStreamUrl(
            String callPublicId) {

        String configuredUrl =
                telephonyMediaProperties
                        .getStreamUrl();

        if (configuredUrl == null
                || configuredUrl.isBlank()) {

            throw new IllegalStateException(
                    "Telephony media stream URL is not configured."
            );
        }

        /*
         * We do not put credentials in the WSS URL.
         *
         * The callPublicId is useful for correlating the
         * WebSocket connection with our Call record.
         */

        String separator =
                configuredUrl.contains("?")
                        ? "&"
                        : "?";

        return configuredUrl
                + separator
                + "callPublicId="
                + callPublicId;
    }

    /**
     * Validates the Agent outbound call request.
     *
     * @param request agent outbound call request
     */
    private void validateRequest(
            PlaceAgentOutboundCallRequestDto request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Agent outbound call request is required."
            );
        }

        if (request.getFlowPublicId() == null
                || request.getFlowPublicId().isBlank()) {

            throw new IllegalArgumentException(
                    "Flow public ID is required."
            );
        }

        if (request.getPhoneNumberPublicId() == null
                || request
                .getPhoneNumberPublicId()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Phone number public ID is required."
            );
        }

        if (request.getToNumber() == null
                || request.getToNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Destination phone number is required."
            );
        }
    }
}
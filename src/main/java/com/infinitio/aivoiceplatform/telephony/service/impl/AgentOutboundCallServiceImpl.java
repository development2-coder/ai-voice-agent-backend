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
import com.infinitio.aivoiceplatform.telephony.config.ExotelProperties;
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
import java.util.UUID;

/**
 * Starts a direct outbound call for an Agent Flow.
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

    private static final String EXOTEL = "EXOTEL";

    private static final String OUTBOUND = "OUTBOUND";

    private static final String INITIATED = "INITIATED";

    private final FlowRepository flowRepository;

    private final PhoneNumberRepository phoneNumberRepository;

    private final CallRepository callRepository;

    private final CallSessionCreateService
            callSessionCreateService;

    private final TelephonyService telephonyService;

    private final CurrentUserService
            currentUserService;

    private final ExotelProperties exotelProperties;

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
                                request
                                        .getPhoneNumberPublicId()
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

        if (!EXOTEL.equalsIgnoreCase(
                phoneNumber.getProvider()
        )) {

            throw new IllegalStateException(
                    "Phone number provider must be EXOTEL."
            );
        }

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
         */
        Call call =
                Call.builder()
                        .campaignContact(null)
                        .provider(EXOTEL)
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
         * BUILD REALTIME STREAM URL
         * ---------------------------------------------------------
         */

        String streamUrl =
                buildStreamUrl(
                        savedCall.getPublicId()
                );

        /*
         * ---------------------------------------------------------
         * PLACE EXOTEL CALL
         * ---------------------------------------------------------
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
                        .callbackUrl(
                                exotelProperties
                                        .getStatusCallbackUrl()
                        )
                        .streamUrl(
                                streamUrl
                        )
                        .streamType(
                                exotelProperties
                                        .getStreamType()
                        )
                        .record(
                                exotelProperties
                                        .getRecord()
                        )
                        .recordingChannels(
                                exotelProperties
                                        .getRecordingChannels()
                        )
                        .timeLimit(
                                exotelProperties
                                        .getTimeLimit()
                        )
                        .build();

        ProviderCallResponseDto
                providerResponse;

        try {

            providerResponse =
                    telephonyService
                            .placeOutboundCall(
                                    EXOTEL,
                                    providerRequest
                            );

        } catch (Exception exception) {

            log.error(
                    "Failed to place direct Agent outbound "
                            + "call. callPublicId={}",
                    savedCall.getPublicId(),
                    exception
            );

            throw exception;
        }

        log.info(
                "Direct Agent outbound call initiated. "
                        + "callPublicId={}, providerCallId={}, "
                        + "flowPublicId={}, agentPublicId={}",
                savedCall.getPublicId(),
                providerResponse
                        .getProviderCallId(),
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

    private String buildStreamUrl(
            String callPublicId) {

        String configuredUrl =
                exotelProperties
                        .getStreamUrl();

        if (configuredUrl == null
                || configuredUrl.isBlank()) {

            throw new IllegalStateException(
                    "EXOTEL_STREAM_URL is not configured."
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
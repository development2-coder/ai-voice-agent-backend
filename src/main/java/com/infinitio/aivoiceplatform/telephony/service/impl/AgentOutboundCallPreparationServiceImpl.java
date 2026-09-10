package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceAgentOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.service.AgentOutboundCallPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Creates the persistent state required before an Agent outbound call.
 *
 * <p>
 * The Call and CallSession are committed before the telephony
 * provider is contacted. This ensures that a provider WebSocket
 * can immediately resolve the CallSession.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOutboundCallPreparationServiceImpl
        implements AgentOutboundCallPreparationService {

    private static final String OUTBOUND = "OUTBOUND";

    private static final String INITIATED = "INITIATED";

    private final CallRepository callRepository;

    private final CallSessionCreateService
            callSessionCreateService;

    private final CurrentUserService currentUserService;

    /**
     * Creates the Call and CallSession before the provider
     * outbound call is initiated.
     *
     * @param request outbound call request
     * @param flow selected Agent flow
     * @param providerCode telephony provider code
     * @param fromNumber configured caller number
     * @return persisted Call
     */
    @Override
    @Transactional
    public Call prepareOutboundCall(
            PlaceAgentOutboundCallRequestDto request,
            Flow flow,
            String providerCode,
            String fromNumber) {

        Call call =
                Call.builder()
                        .campaignContact(null)
                        .provider(providerCode)
                        .fromNumber(fromNumber)
                        .toNumber(request.getToNumber())
                        .direction(OUTBOUND)
                        .status(INITIATED)
                        .startedAt(LocalDateTime.now())
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

        CreateCallSessionRequestDto
                callSessionRequest =
                CreateCallSessionRequestDto
                        .builder()
                        .callId(
                                savedCall.getPublicId()
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

        log.info(
                "CallSession created successfully before "
                        + "provider call. callPublicId={}",
                savedCall.getPublicId()
        );

        return savedCall;
    }
}
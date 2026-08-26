package com.infinitio.aivoiceplatform.callsession.service.impl;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionRuntimeService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runtime entry point for Call Session creation.
 *
 * <p>
 * This service only prepares the runtime Call Session
 * request and delegates persistence to the existing
 * CallSessionCreateService.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallSessionRuntimeServiceImpl
        implements CallSessionRuntimeService {

    private final CallSessionCreateService
            callSessionCreateService;

    /**
     * Starts a runtime Call Session.
     *
     * <p>
     * The initial flow node is intentionally not supplied.
     * FlowExecutionService is responsible for resolving the
     * START node from the selected Flow.
     * </p>
     *
     * @param callPublicId platform Call public identifier
     * @param tenantPublicId tenant public identifier
     * @param agentPublicId agent public identifier
     * @param agentVersion runtime configuration version
     * @param flowPublicId flow public identifier
     * @param language session language
     * @param createdBy audit user ID
     * @return created Call Session
     */
    @Override
    public CallSessionResponseDto startSession(
            String callPublicId,
            String tenantPublicId,
            String agentPublicId,
            Integer agentVersion,
            String flowPublicId,
            String language,
            Long createdBy) {

        log.info(
                "Starting Call Session runtime. "
                        + "callId={}, tenantId={}, agentId={}, "
                        + "agentVersion={}, flowPublicId={}",
                callPublicId,
                tenantPublicId,
                agentPublicId,
                agentVersion,
                flowPublicId
        );

        validate(
                callPublicId,
                tenantPublicId,
                agentPublicId,
                agentVersion,
                flowPublicId,
                createdBy
        );

        CreateCallSessionRequestDto request =
                CreateCallSessionRequestDto.builder()
                        .callId(
                                callPublicId
                        )
                        .tenantId(
                                tenantPublicId
                        )
                        .agentId(
                                agentPublicId
                        )
                        .agentVersion(
                                agentVersion
                        )
                        .flowPublicId(
                                flowPublicId
                        )
                        /*
                         * Do not hard-code START.
                         *
                         * FlowExecutionService resolves the
                         * actual START node from the Flow.
                         */
                        .flowNodeId(
                                null
                        )
                        .language(
                                language
                        )
                        .build();

        return callSessionCreateService
                .createCallSession(
                        request,
                        createdBy
                );
    }

    /**
     * Validates runtime session input.
     *
     * @param callPublicId Call public ID
     * @param tenantPublicId tenant public ID
     * @param agentPublicId agent public ID
     * @param agentVersion runtime version
     * @param flowPublicId Flow public ID
     * @param createdBy audit user ID
     */
    private void validate(
            String callPublicId,
            String tenantPublicId,
            String agentPublicId,
            Integer agentVersion,
            String flowPublicId,
            Long createdBy) {

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (tenantPublicId == null
                || tenantPublicId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .TENANT_ID_REQUIRED
            );
        }

        if (agentPublicId == null
                || agentPublicId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_ID_REQUIRED
            );
        }

        if (agentVersion == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_VERSION_REQUIRED
            );
        }

        if (agentVersion <= 0) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_VERSION_INVALID
            );
        }

        if (flowPublicId == null
                || flowPublicId.isBlank()) {

            throw new BadRequestException(
                    "Flow public ID is required."
            );
        }

        if (createdBy == null) {

            throw new BadRequestException(
                    "Created by user is required."
            );
        }
    }
}
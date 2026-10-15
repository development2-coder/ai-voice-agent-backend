package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerStatus;
import com.infinitio.aivoiceplatform.aidialer.dto.request.CreateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.PauseDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.ResumeDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.StartDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.UpdateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerResponse;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.mapper.AiDialerMapper;
import com.infinitio.aivoiceplatform.aidialer.repository.AiDialerRepository;
import com.infinitio.aivoiceplatform.aidialer.service.AiDialerService;
import com.infinitio.aivoiceplatform.aidialer.validator.AiDialerValidator;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Dialer Service Implementation.
 *
 * Handles AI Dialer creation, update, retrieval,
 * lifecycle operations and campaign association.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiDialerServiceImpl
        implements AiDialerService {

    private static final Integer ACTIVE = 1;

    private static final Integer NOT_DELETED = 0;


    private final AiDialerRepository aiDialerRepository;

    private final AiDialerMapper aiDialerMapper;

    private final AiDialerValidator aiDialerValidator;

    private final CampaignValidator campaignValidator;

    private final AgentValidator agentValidator;

    private final FlowValidator flowValidator;

    private final TenantAccessService tenantAccessService;

    private final CurrentUserService
            currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public DialerResponse create(
            CreateAiDialerRequest request) {

        log.info(
                "Creating AI Dialer : {}",
                request.getDialerName()
        );

        aiDialerValidator.validateForCreate(
                request
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        request.getCampaignPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        tenantAccessService.validateCampaignAccess(campaign);
        tenantAccessService.validateAgentAccess(agent);
        tenantAccessService.validateFlowAccess(flow);

        if (campaign.getAgent() == null
                || !agent.getId().equals(campaign.getAgent().getId())) {
            throw new BadRequestException(
                    "The selected Campaign does not belong to the selected Agent."
            );
        }

        if (flow.getAgent() == null
                || !agent.getId().equals(flow.getAgent().getId())) {
            throw new BadRequestException(
                    "The selected Flow does not belong to the selected Agent."
            );
        }

        AiDialer dialer =
                aiDialerMapper.toEntity(
                        request
                );

        dialer.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        /*
         * Relationship entities are resolved through
         * their validators and assigned to the entity.
         */
        dialer.setCampaign(campaign);
        dialer.setAgent(agent);
        dialer.setFlow(flow);

        dialer.setStatus(
                request.getScheduledStartAt() != null
                        ? DialerStatus.SCHEDULED
                        : DialerStatus.DRAFT
        );

        dialer.setIsActive(ACTIVE);
        dialer.setIsDeleted(NOT_DELETED);

        AiDialer savedDialer =
                aiDialerRepository.save(
                        dialer
                );

        log.info(
                "AI Dialer created successfully : {}",
                savedDialer.getPublicId()
        );

        return aiDialerMapper.toResponse(
                savedDialer
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DialerResponse getByPublicId(
            String publicId) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        publicId
                );

        validateDialerAccess(dialer);

        return aiDialerMapper.toResponse(
                dialer
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<DialerResponse> getAll() {

        List<AiDialer> dialers;

        if (tenantAccessService.isSuperAdmin()) {
            dialers = aiDialerRepository.findAllByIsDeleted(NOT_DELETED);
        } else {
            dialers =
                    aiDialerRepository.findAllByAgent_Tenant_IdAndIsDeleted(
                            tenantAccessService.getCurrentTenantId(),
                            NOT_DELETED
                    );
        }

        return dialers.stream()
                .map(aiDialerMapper::toResponse)
                .toList();
    }


    // =========================================================
    // GET BY CAMPAIGN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<DialerResponse> getByCampaign(
            String campaignPublicId) {

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        tenantAccessService.validateCampaignAccess(campaign);

        return aiDialerRepository
                .findAllByCampaignIdAndIsDeleted(
                        campaign.getId(),
                        NOT_DELETED
                )
                .stream()
                .map(aiDialerMapper::toResponse)
                .toList();
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public DialerResponse update(
            String publicId,
            UpdateAiDialerRequest request) {

        log.info(
                "Updating AI Dialer : {}",
                publicId
        );

        aiDialerValidator.validateForUpdate(
                request
        );

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        publicId
                );

        validateDialerAccess(dialer);

        aiDialerMapper.updateEntity(
                request,
                dialer
        );

        if (request.getCampaignPublicId() != null
                && !request.getCampaignPublicId().isBlank()) {

            Campaign campaign =
                    campaignValidator.validateAndGet(
                            request.getCampaignPublicId()
                    );

            dialer.setCampaign(
                    campaign
            );
        }

        if (request.getAgentPublicId() != null
                && !request.getAgentPublicId().isBlank()) {

            Agent agent =
                    agentValidator.validateAndGet(
                            request.getAgentPublicId()
                    );

            dialer.setAgent(
                    agent
            );
        }

        if (request.getFlowPublicId() != null
                && !request.getFlowPublicId().isBlank()) {

            Flow flow =
                    flowValidator.validateAndGet(
                            request.getFlowPublicId()
                    );

            dialer.setFlow(
                    flow
            );
        }

        /*
         * ---------------------------------------------------------
         * UPDATE DIALER STATUS
         * ---------------------------------------------------------
         *
         * If the user provides a schedule, the dialer must
         * become SCHEDULED so that DialerSchedulerServiceImpl
         * can pick it up automatically.
         */
        if (request.getScheduledStartAt() != null) {

            dialer.setStatus(
                    DialerStatus.SCHEDULED
            );

            dialer.setStartedAt(null);
            dialer.setPausedAt(null);
            dialer.setCompletedAt(null);

            log.info(
                    "AI Dialer scheduled successfully. "
                            + "Public Id : {}, "
                            + "Start : {}, "
                            + "End : {}",
                    publicId,
                    request.getScheduledStartAt(),
                    request.getScheduledEndAt()
            );

        } else if (
                dialer.getStatus()
                        == DialerStatus.SCHEDULED
        ) {

            /*
             * If schedule is removed from an existing
             * scheduled dialer, return it to DRAFT.
             */
            dialer.setStatus(
                    DialerStatus.DRAFT
            );

            dialer.setStartedAt(null);
            dialer.setCompletedAt(null);

            log.info(
                    "AI Dialer schedule removed. "
                            + "Moving dialer to DRAFT. "
                            + "Public Id : {}",
                    publicId
            );
        }

        AiDialer updatedDialer =
                aiDialerRepository.save(
                        dialer
                );

        log.info(
                "AI Dialer updated successfully : {}",
                updatedDialer.getPublicId()
        );

        return aiDialerMapper.toResponse(
                updatedDialer
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        publicId
                );

        validateDialerAccess(dialer);

        /*
         * A deleted dialer must not continue running.
         */
        if (dialer.getStatus()
                == DialerStatus.RUNNING
                || dialer.getStatus()
                == DialerStatus.PAUSED
                || dialer.getStatus()
                == DialerStatus.SCHEDULED) {

            dialer.setStatus(
                    DialerStatus.STOPPED
            );
        }

        dialer.setIsDeleted(
                NOT_DELETED + 1
        );

        dialer.setIsActive(
                NOT_DELETED
        );

        aiDialerRepository.save(
                dialer
        );

        log.info(
                "AI Dialer deleted successfully : {}",
                publicId
        );
    }


    // =========================================================
    // START
    // =========================================================

    @Override
    public DialerResponse start(
            StartDialerRequest request) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        request.getDialerPublicId()
                );

        validateDialerAccess(dialer);

        if (dialer.getStatus()
                == DialerStatus.RUNNING) {

            throw new IllegalStateException(
                    "Dialer is already running."
            );
        }

        if (dialer.getStatus()
                == DialerStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed dialer cannot be started again."
            );
        }

        dialer.setStatus(
                DialerStatus.RUNNING
        );

        dialer.setStartedAt(
                LocalDateTime.now()
        );

        dialer.setPausedAt(null);
        dialer.setCompletedAt(null);

        AiDialer savedDialer =
                aiDialerRepository.save(
                        dialer
                );

        log.info(
                "AI Dialer started : {}",
                savedDialer.getPublicId()
        );

        return aiDialerMapper.toResponse(
                savedDialer
        );
    }


    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    public DialerResponse pause(
            PauseDialerRequest request) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        request.getDialerPublicId()
                );

        validateDialerAccess(dialer);

        if (dialer.getStatus()
                != DialerStatus.RUNNING) {

            throw new IllegalStateException(
                    "Only a running dialer can be paused."
            );
        }

        dialer.setStatus(
                DialerStatus.PAUSED
        );

        dialer.setPausedAt(
                LocalDateTime.now()
        );

        AiDialer savedDialer =
                aiDialerRepository.save(
                        dialer
                );

        log.info(
                "AI Dialer paused : {}",
                savedDialer.getPublicId()
        );

        return aiDialerMapper.toResponse(
                savedDialer
        );
    }


    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public DialerResponse resume(
            ResumeDialerRequest request) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        request.getDialerPublicId()
                );

        validateDialerAccess(dialer);

        if (dialer.getStatus()
                != DialerStatus.PAUSED) {

            throw new IllegalStateException(
                    "Only a paused dialer can be resumed."
            );
        }

        dialer.setStatus(
                DialerStatus.RUNNING
        );

        dialer.setPausedAt(null);

        AiDialer savedDialer =
                aiDialerRepository.save(
                        dialer
                );

        log.info(
                "AI Dialer resumed : {}",
                savedDialer.getPublicId()
        );

        return aiDialerMapper.toResponse(
                savedDialer
        );
    }


    // =========================================================
    // STOP
    // =========================================================

    @Override
    public DialerResponse stop(
            String publicId) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        publicId
                );

        validateDialerAccess(dialer);

        if (dialer.getStatus()
                == DialerStatus.STOPPED) {

            throw new IllegalStateException(
                    "Dialer is already stopped."
            );
        }

        if (dialer.getStatus()
                == DialerStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Completed dialer cannot be stopped."
            );
        }

        dialer.setStatus(
                DialerStatus.STOPPED
        );

        dialer.setCompletedAt(
                LocalDateTime.now()
        );

        AiDialer savedDialer =
                aiDialerRepository.save(
                        dialer
                );

        log.info(
                "AI Dialer stopped : {}",
                savedDialer.getPublicId()
        );

        return aiDialerMapper.toResponse(
                savedDialer
        );
    }

    /**
     * Validates access to an existing AI Dialer and its related resources.
     *
     * @param dialer AI Dialer to validate
     */
    private void validateDialerAccess(AiDialer dialer) {

        if (dialer == null) {
            throw new BadRequestException(
                    "AI Dialer is required."
            );
        }

        validateDialerReferences(
                dialer.getCampaign(),
                dialer.getAgent(),
                dialer.getFlow()
        );
    }

    /**
     * Validates tenant access and ownership consistency for the complete
     * Campaign, Agent and Flow relationship.
     *
     * <p>Relationship requirements:</p>
     * <ul>
     *     <li>The current user must have access to the Campaign.</li>
     *     <li>The current user must have access to the Agent.</li>
     *     <li>The current user must have access to the Flow.</li>
     *     <li>The Campaign must belong to the selected Agent.</li>
     *     <li>The Flow must belong to the selected Agent.</li>
     * </ul>
     *
     * @param campaign campaign associated with the dialer
     * @param agent agent associated with the dialer
     * @param flow flow associated with the dialer
     */
    private void validateDialerReferences(
            Campaign campaign,
            Agent agent,
            Flow flow) {

        if (campaign == null || agent == null || flow == null) {
            throw new BadRequestException(
                    "AI Dialer must have a Campaign, Agent and Flow."
            );
        }

        /*
         * Validate each resource independently. This is essential
         * when an update supplies replacement resource IDs.
         */
        tenantAccessService.validateCampaignAccess(campaign);
        tenantAccessService.validateAgentAccess(agent);
        tenantAccessService.validateFlowAccess(flow);

        /*
         * Ensure the selected Campaign belongs to the selected Agent.
         */
        if (campaign.getAgent() == null
                || campaign.getAgent().getId() == null
                || agent.getId() == null
                || !agent.getId().equals(campaign.getAgent().getId())) {

            throw new BadRequestException(
                    "The selected Campaign does not belong to the selected Agent."
            );
        }

        /*
         * Ensure the selected Flow belongs to the selected Agent.
         */
        if (flow.getAgent() == null
                || flow.getAgent().getId() == null
                || agent.getId() == null
                || !agent.getId().equals(flow.getAgent().getId())) {

            throw new BadRequestException(
                    "The selected Flow does not belong to the selected Agent."
            );
        }
    }
}
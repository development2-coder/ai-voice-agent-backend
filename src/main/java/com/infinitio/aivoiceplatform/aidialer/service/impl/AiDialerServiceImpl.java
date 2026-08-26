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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        AiDialer dialer =
                aiDialerMapper.toEntity(
                        request
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

        return aiDialerRepository
                .findAllByIsDeleted(
                        NOT_DELETED
                )
                .stream()
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

        /*
         * Map only normal scalar fields.
         *
         * Relationship fields are ignored by MapStruct
         * and resolved separately below.
         */
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
}
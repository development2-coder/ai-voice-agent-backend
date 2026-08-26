package com.infinitio.aivoiceplatform.tts.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.tts.dto.request.CreateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.request.UpdateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.response.TtsResponse;
import com.infinitio.aivoiceplatform.tts.entity.Tts;
import com.infinitio.aivoiceplatform.tts.mapper.TtsMapper;
import com.infinitio.aivoiceplatform.tts.repository.TtsRepository;
import com.infinitio.aivoiceplatform.tts.service.TtsService;
import com.infinitio.aivoiceplatform.tts.validator.TtsValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for TTS.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TtsServiceImpl
        implements TtsService {

    private final TtsRepository ttsRepository;

    private final TtsMapper ttsMapper;

    private final TtsValidator ttsValidator;

    private final AgentValidator agentValidator;

    private final CurrentUserService currentUserService;

    /**
     * Creates a new TTS configuration.
     *
     * @param request create TTS request
     * @return created TTS response
     */
    @Override
    public TtsResponse create(
            CreateTtsRequest request) {

        log.info(
                "Creating TTS. Code : {}, Provider : {}, Model : {}",
                request != null
                        ? request.getTtsCode()
                        : null,
                request != null
                        ? request.getProvider()
                        : null,
                request != null
                        ? request.getModel()
                        : null
        );

        /*
         * Validate TTS create request.
         */
        ttsValidator.validateForCreate(
                request
        );

        /*
         * Validate and retrieve the associated agent.
         */
        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Convert request into entity.
         *
         * Audit and system fields are intentionally ignored
         * by the mapper and are populated here.
         */
        Tts tts =
                ttsMapper.toEntity(
                        request
                );

        tts.setAgent(
                agent
        );

        /*
         * Audit information comes from the authenticated user.
         *
         * BaseEntity.createdBy is mandatory in the database.
         */
        tts.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        Tts savedTts =
                ttsRepository.save(
                        tts
                );

        log.info(
                "TTS created successfully. Public Id : {}",
                savedTts.getPublicId()
        );

        return ttsMapper.toResponse(
                savedTts
        );
    }

    /**
     * Updates an existing TTS configuration.
     *
     * @param request update TTS request
     * @return updated TTS response
     */
    @Override
    public TtsResponse update(
            UpdateTtsRequest request) {

        log.info(
                "Updating TTS. Public Id : {}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        /*
         * Validate update request.
         */
        ttsValidator.validateForUpdate(
                request
        );

        /*
         * Retrieve existing TTS configuration.
         */
        Tts tts =
                ttsValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Validate and retrieve the new associated agent.
         */
        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Update editable fields only.
         */
        ttsMapper.updateEntity(
                request,
                tts
        );

        tts.setAgent(
                agent
        );

        /*
         * Audit information comes from the authenticated user.
         */
        tts.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        Tts updatedTts =
                ttsRepository.save(
                        tts
                );

        log.info(
                "TTS updated successfully. Public Id : {}",
                updatedTts.getPublicId()
        );

        return ttsMapper.toResponse(
                updatedTts
        );
    }

    /**
     * Retrieves a TTS configuration by public identifier.
     *
     * @param publicId TTS public identifier
     * @return TTS response
     */
    @Override
    @Transactional(readOnly = true)
    public TtsResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        return ttsMapper.toResponse(
                tts
        );
    }

    /**
     * Retrieves paginated TTS configurations.
     *
     * @param page page number
     * @param size page size
     * @return paginated TTS response
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TtsResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching TTS configurations. Page : {}, Size : {}",
                page,
                size
        );

        Page<Tts> result =
                ttsRepository.findAll(
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<TtsResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        ttsMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        result.getNumber()
                )
                .pageSize(
                        result.getSize()
                )
                .totalPages(
                        result.getTotalPages()
                )
                .totalElements(
                        result.getTotalElements()
                )
                .first(
                        result.isFirst()
                )
                .last(
                        result.isLast()
                )
                .build();
    }

    /**
     * Soft deletes a TTS configuration.
     *
     * @param publicId TTS public identifier
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Soft delete with authenticated user.
         */
        tts.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        ttsRepository.save(
                tts
        );

        log.info(
                "TTS deleted successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Activates a TTS configuration.
     *
     * @param publicId TTS public identifier
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Activation audit information comes from
         * the authenticated user.
         */
        tts.activate(
                currentUserService.getCurrentUserId()
        );

        ttsRepository.save(
                tts
        );

        log.info(
                "TTS activated successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Deactivates a TTS configuration.
     *
     * @param publicId TTS public identifier
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Deactivation audit information comes from
         * the authenticated user.
         */
        tts.deactivate(
                currentUserService.getCurrentUserId()
        );

        ttsRepository.save(
                tts
        );

        log.info(
                "TTS deactivated successfully. Public Id : {}",
                publicId
        );
    }
}
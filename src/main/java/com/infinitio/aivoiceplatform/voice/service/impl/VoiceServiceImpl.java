package com.infinitio.aivoiceplatform.voice.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.voice.dto.request.CreateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.request.UpdateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.response.VoiceResponse;
import com.infinitio.aivoiceplatform.voice.entity.Voice;
import com.infinitio.aivoiceplatform.voice.mapper.VoiceMapper;
import com.infinitio.aivoiceplatform.voice.repository.VoiceRepository;
import com.infinitio.aivoiceplatform.voice.service.VoiceService;
import com.infinitio.aivoiceplatform.voice.validator.VoiceValidator;
import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Voice.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VoiceServiceImpl implements VoiceService {

    private final VoiceRepository voiceRepository;

    private final VoiceMapper voiceMapper;

    private final VoiceValidator voiceValidator;

    private final AgentValidator agentValidator;

    private final TenantAccessService tenantAccessService;

    @Override
    public VoiceResponse create(CreateVoiceRequest request) {

        log.info(
                "Creating Voice. Voice Code : {}",
                request.getVoiceCode()
        );

        voiceValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        tenantAccessService.validateAgentAccess(agent);

        Voice voice =
                voiceMapper.toEntity(request);

        voice.setAgent(agent);

        Voice savedVoice =
                voiceRepository.save(voice);

        log.info(
                "Voice created successfully. Public Id : {}",
                savedVoice.getPublicId()
        );

        return voiceMapper.toResponse(savedVoice);
    }

    @Override
    public VoiceResponse update(UpdateVoiceRequest request) {

        log.info(
                "Updating Voice. Public Id : {}",
                request.getPublicId()
        );

        voiceValidator.validateForUpdate(request);

        Voice voice =
                voiceValidator.validateAndGet(
                        request.getPublicId()
                );

        tenantAccessService.validateAgentAccess(
                voice.getAgent()
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        tenantAccessService.validateAgentAccess(agent);

        voiceMapper.updateEntity(
                request,
                voice
        );

        voice.setAgent(agent);

        Voice updatedVoice =
                voiceRepository.save(voice);

        log.info(
                "Voice updated successfully. Public Id : {}",
                updatedVoice.getPublicId()
        );

        return voiceMapper.toResponse(updatedVoice);
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Voice. Public Id : {}",
                publicId
        );

        Voice voice =
                voiceValidator.validateAndGet(publicId);

        tenantAccessService.validateAgentAccess(
                voice.getAgent()
        );

        return voiceMapper.toResponse(voice);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VoiceResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Voices. Page : {}, Size : {}",
                page,
                size
        );

        Page<Voice> result;

        if (tenantAccessService.isSuperAdmin()) {

            result = voiceRepository.findByIsDeleted(
                    NOT_DELETED,
                    PageRequest.of(page, size)
            );

        } else {

            result = voiceRepository
                    .findByAgent_Tenant_IdAndIsDeleted(
                            tenantAccessService.getCurrentTenantId(),
                            NOT_DELETED,
                            PageRequest.of(page, size)
                    );
        }

        return PageResponse.<VoiceResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(voiceMapper::toResponse)
                                .toList()
                )
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting Voice. Public Id : {}",
                publicId
        );

        Voice voice =
                voiceValidator.validateAndGet(publicId);

        tenantAccessService.validateAgentAccess(
                voice.getAgent()
        );

        voice.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        voiceRepository.save(voice);

        log.info(
                "Voice deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Voice. Public Id : {}",
                publicId
        );

        Voice voice =
                voiceValidator.validateAndGet(publicId);

        tenantAccessService.validateAgentAccess(
                voice.getAgent()
        );

        voice.activate(
                tenantAccessService.getCurrentUserId()
        );

        voiceRepository.save(voice);

        log.info(
                "Voice activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Voice. Public Id : {}",
                publicId
        );

        Voice voice =
                voiceValidator.validateAndGet(publicId);

        tenantAccessService.validateAgentAccess(
                voice.getAgent()
        );

        voice.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        voiceRepository.save(voice);

        log.info(
                "Voice deactivated successfully. Public Id : {}",
                publicId
        );
    }
}
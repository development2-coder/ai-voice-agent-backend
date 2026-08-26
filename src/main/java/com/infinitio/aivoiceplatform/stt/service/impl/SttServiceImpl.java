package com.infinitio.aivoiceplatform.stt.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.stt.dto.request.CreateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.request.UpdateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.response.SttResponse;
import com.infinitio.aivoiceplatform.stt.entity.Stt;
import com.infinitio.aivoiceplatform.stt.mapper.SttMapper;
import com.infinitio.aivoiceplatform.stt.repository.SttRepository;
import com.infinitio.aivoiceplatform.stt.service.SttService;
import com.infinitio.aivoiceplatform.stt.validator.SttValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for STT.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SttServiceImpl implements SttService {

    private final SttRepository sttRepository;

    private final SttMapper sttMapper;

    private final SttValidator sttValidator;

    private final AgentValidator agentValidator;

    @Override
    public SttResponse create(CreateSttRequest request) {

        log.info(
                "Creating STT. Code : {}, Provider : {}, Model : {}",
                request.getSttCode(),
                request.getProvider(),
                request.getModel()
        );

        sttValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        Stt stt =
                sttMapper.toEntity(request);

        stt.setAgent(agent);

        Stt savedStt =
                sttRepository.save(stt);

        log.info(
                "STT created successfully. Public Id : {}",
                savedStt.getPublicId()
        );

        return sttMapper.toResponse(savedStt);
    }

    @Override
    public SttResponse update(UpdateSttRequest request) {

        log.info(
                "Updating STT. Public Id : {}",
                request.getPublicId()
        );

        sttValidator.validateForUpdate(request);

        Stt stt =
                sttValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        sttMapper.updateEntity(
                request,
                stt
        );

        stt.setAgent(agent);

        Stt updatedStt =
                sttRepository.save(stt);

        log.info(
                "STT updated successfully. Public Id : {}",
                updatedStt.getPublicId()
        );

        return sttMapper.toResponse(updatedStt);
    }

    @Override
    @Transactional(readOnly = true)
    public SttResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching STT. Public Id : {}",
                publicId
        );

        Stt stt =
                sttValidator.validateAndGet(publicId);

        return sttMapper.toResponse(stt);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SttResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching STT configurations. Page : {}, Size : {}",
                page,
                size
        );

        Page<Stt> result =
                sttRepository.findAll(
                        PageRequest.of(page, size)
                );

        return PageResponse.<SttResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(sttMapper::toResponse)
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
                "Deleting STT. Public Id : {}",
                publicId
        );

        Stt stt =
                sttValidator.validateAndGet(publicId);

        stt.markAsDeleted(1L);

        sttRepository.save(stt);

        log.info(
                "STT deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating STT. Public Id : {}",
                publicId
        );

        Stt stt =
                sttValidator.validateAndGet(publicId);

        stt.activate(1L);

        sttRepository.save(stt);

        log.info(
                "STT activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating STT. Public Id : {}",
                publicId
        );

        Stt stt =
                sttValidator.validateAndGet(publicId);

        stt.deactivate(1L);

        sttRepository.save(stt);

        log.info(
                "STT deactivated successfully. Public Id : {}",
                publicId
        );
    }
}
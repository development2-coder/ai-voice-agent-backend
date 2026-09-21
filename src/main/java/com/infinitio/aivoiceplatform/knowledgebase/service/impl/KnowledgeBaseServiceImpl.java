package com.infinitio.aivoiceplatform.knowledgebase.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.knowledgebase.constant.KnowledgeBaseMessages;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.CreateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.UpdateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.response.KnowledgeBaseResponse;
import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import com.infinitio.aivoiceplatform.knowledgebase.mapper.KnowledgeBaseMapper;
import com.infinitio.aivoiceplatform.knowledgebase.repository.KnowledgeBaseRepository;
import com.infinitio.aivoiceplatform.knowledgebase.service.KnowledgeBaseService;
import com.infinitio.aivoiceplatform.knowledgebase.validator.KnowledgeBaseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Knowledge Base.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBaseServiceImpl
        implements KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeBaseValidator knowledgeBaseValidator;

    private final AgentValidator agentValidator;

    @Override
    public KnowledgeBaseResponse create(
            CreateKnowledgeBaseRequest request) {

        log.info(
                "Creating Knowledge Base. Code : {}",
                request.getKnowledgeBaseCode()
        );

        knowledgeBaseValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        KnowledgeBase knowledgeBase =
                knowledgeBaseMapper.toEntity(request);

        knowledgeBase.setAgent(agent);

        KnowledgeBase savedKnowledgeBase =
                knowledgeBaseRepository.save(knowledgeBase);

        log.info(
                "Knowledge Base created successfully. Public Id : {}",
                savedKnowledgeBase.getPublicId()
        );

        return knowledgeBaseMapper.toResponse(
                savedKnowledgeBase
        );
    }

    @Override
    public KnowledgeBaseResponse update(
            UpdateKnowledgeBaseRequest request) {

        log.info(
                "Updating Knowledge Base. Public Id : {}",
                request.getPublicId()
        );

        knowledgeBaseValidator.validateForUpdate(request);

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        knowledgeBaseMapper.updateEntity(
                request,
                knowledgeBase
        );

        knowledgeBase.setAgent(agent);

        KnowledgeBase updatedKnowledgeBase =
                knowledgeBaseRepository.save(
                        knowledgeBase
                );

        log.info(
                "Knowledge Base updated successfully. Public Id : {}",
                updatedKnowledgeBase.getPublicId()
        );

        return knowledgeBaseMapper.toResponse(
                updatedKnowledgeBase
        );
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeBaseResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Knowledge Base. Public Id : {}",
                publicId
        );

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        publicId
                );

        return knowledgeBaseMapper.toResponse(
                knowledgeBase
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KnowledgeBaseResponse> getAll() {

        log.info(
                "Fetching all Knowledge Bases"
        );

        List<KnowledgeBase> knowledgeBases =
                knowledgeBaseRepository.findAll();

        List<KnowledgeBaseResponse> content =
                knowledgeBases.stream()
                        .map(
                                knowledgeBaseMapper::toResponse
                        )
                        .toList();

        int totalElements =
                content.size();

        return PageResponse
                .<KnowledgeBaseResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(
                        totalElements == 0
                                ? 0
                                : 1
                )
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }

    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting Knowledge Base. Public Id : {}",
                publicId
        );

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        publicId
                );

        knowledgeBase.markAsDeleted(1L);

        knowledgeBaseRepository.save(
                knowledgeBase
        );

        log.info(
                "Knowledge Base deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Knowledge Base. Public Id : {}",
                publicId
        );

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        publicId
                );

        knowledgeBase.activate(1L);

        knowledgeBaseRepository.save(
                knowledgeBase
        );

        log.info(
                "Knowledge Base activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Knowledge Base. Public Id : {}",
                publicId
        );

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        publicId
                );

        knowledgeBase.deactivate(1L);

        knowledgeBaseRepository.save(
                knowledgeBase
        );

        log.info(
                "Knowledge Base deactivated successfully. Public Id : {}",
                publicId
        );
    }
}
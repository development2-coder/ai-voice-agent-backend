package com.infinitio.aivoiceplatform.knowledgebase.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBaseServiceImpl
        implements KnowledgeBaseService {

    private static final Integer NOT_DELETED = 0;

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeBaseValidator knowledgeBaseValidator;

    private final AgentValidator agentValidator;

    private final TenantAccessService tenantAccessService;

    @Override
    public KnowledgeBaseResponse create(
            CreateKnowledgeBaseRequest request) {

        log.info(
                "Creating Knowledge Base. Code: {}",
                request.getKnowledgeBaseCode()
        );

        knowledgeBaseValidator.validateForCreate(request);

        Agent agent = agentValidator.validateAndGet(
                request.getAgentPublicId()
        );

        // Prevent creating a Knowledge Base under another tenant's Agent.
        tenantAccessService.validateAgentAccess(agent);

        KnowledgeBase knowledgeBase =
                knowledgeBaseMapper.toEntity(request);

        knowledgeBase.setAgent(agent);
        knowledgeBase.setCreatedBy(
                tenantAccessService.getCurrentUserId()
        );

        KnowledgeBase savedKnowledgeBase =
                knowledgeBaseRepository.save(knowledgeBase);

        log.info(
                "Knowledge Base created. Public ID: {}",
                savedKnowledgeBase.getPublicId()
        );

        return knowledgeBaseMapper.toResponse(savedKnowledgeBase);
    }

    @Override
    public KnowledgeBaseResponse update(
            UpdateKnowledgeBaseRequest request) {

        log.info(
                "Updating Knowledge Base. Public ID: {}",
                request.getPublicId()
        );

        knowledgeBaseValidator.validateForUpdate(request);

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(
                        request.getPublicId()
                );

        // Validate the existing record before changing its Agent.
        tenantAccessService.validateKnowledgeBaseAccess(
                knowledgeBase
        );

        Agent newAgent = agentValidator.validateAndGet(
                request.getAgentPublicId()
        );

        // Prevent moving a record to an inaccessible tenant.
        tenantAccessService.validateAgentAccess(newAgent);

        knowledgeBaseMapper.updateEntity(
                request,
                knowledgeBase
        );

        knowledgeBase.setAgent(newAgent);
        knowledgeBase.setUpdatedBy(
                tenantAccessService.getCurrentUserId()
        );

        KnowledgeBase updatedKnowledgeBase =
                knowledgeBaseRepository.save(knowledgeBase);

        return knowledgeBaseMapper.toResponse(updatedKnowledgeBase);
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeBaseResponse getByPublicId(String publicId) {

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                knowledgeBase
        );

        return knowledgeBaseMapper.toResponse(knowledgeBase);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KnowledgeBaseResponse> getAll() {

        List<KnowledgeBase> knowledgeBases;

        if (tenantAccessService.isSuperAdmin()) {
            // SUPER_ADMIN can view records across all tenants.
            knowledgeBases =
                    knowledgeBaseRepository.findByIsDeleted(
                            NOT_DELETED
                    );
        } else {
            // ADMIN and USER can view only their own tenant's records.
            Long tenantId =
                    tenantAccessService.getCurrentTenantId();

            knowledgeBases =
                    knowledgeBaseRepository
                            .findByAgent_Tenant_IdAndIsDeleted(
                                    tenantId,
                                    NOT_DELETED
                            );
        }

        List<KnowledgeBaseResponse> content =
                knowledgeBases.stream()
                        .map(knowledgeBaseMapper::toResponse)
                        .toList();

        int totalElements = content.size();

        return PageResponse
                .<KnowledgeBaseResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(totalElements == 0 ? 0 : 1)
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }

    @Override
    public void delete(String publicId) {

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                knowledgeBase
        );

        knowledgeBase.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        knowledgeBaseRepository.save(knowledgeBase);

        log.info("Knowledge Base deleted. Public ID: {}", publicId);
    }

    @Override
    public void activate(String publicId) {

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                knowledgeBase
        );

        knowledgeBase.activate(
                tenantAccessService.getCurrentUserId()
        );

        knowledgeBaseRepository.save(knowledgeBase);

        log.info("Knowledge Base activated. Public ID: {}", publicId);
    }

    @Override
    public void deactivate(String publicId) {

        KnowledgeBase knowledgeBase =
                knowledgeBaseValidator.validateAndGet(publicId);

        tenantAccessService.validateKnowledgeBaseAccess(
                knowledgeBase
        );

        knowledgeBase.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        knowledgeBaseRepository.save(knowledgeBase);

        log.info("Knowledge Base deactivated. Public ID: {}", publicId);
    }
}
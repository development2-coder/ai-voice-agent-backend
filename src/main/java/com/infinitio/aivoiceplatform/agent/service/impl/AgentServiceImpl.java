package com.infinitio.aivoiceplatform.agent.service.impl;

import com.infinitio.aivoiceplatform.agent.dto.request.CreateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.request.UpdateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentResponse;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.mapper.AgentMapper;
import com.infinitio.aivoiceplatform.agent.repository.AgentRepository;
import com.infinitio.aivoiceplatform.agent.service.AgentService;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.validator.TenantValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Agent.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentServiceImpl implements AgentService {

    private static final Integer ACTIVE = 1;
    private static final Integer NOT_DELETED = 0;

    private final AgentRepository agentRepository;

    private final AgentMapper agentMapper;

    private final AgentValidator agentValidator;

    private final TenantValidator tenantValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public AgentResponse create(
            CreateAgentRequest request) {

        log.info(
                "Creating Agent. Agent Code : {}",
                request.getAgentCode()
        );

        log.info(
                "Resolving Tenant for Agent creation. Tenant Public Id : {}",
                request.getTenantPublicId()
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        request.getTenantPublicId()
                );

        agentValidator.validateCreate(
                request,
                tenant.getId()
        );

        Agent agent =
                agentMapper.toEntity(request);

        agent.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        agent.setTenant(tenant);

        /*
         * Tenant belongs to an Organization.
         * Keep Agent organization synchronized with Tenant.
         */
        agent.setOrganization(
                tenant.getOrganization()
        );

        agent.setIsActive(ACTIVE);

        agent.setIsDeleted(NOT_DELETED);

        Agent savedAgent =
                agentRepository.save(agent);

        log.info(
                "Agent created successfully. Public Id : {}",
                savedAgent.getPublicId()
        );

        return agentMapper.toResponse(savedAgent);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public AgentResponse update(
            UpdateAgentRequest request) {

        log.info(
                "Updating Agent. Public Id : {}",
                request.getPublicId()
        );

        Agent existingAgent =
                agentValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * If tenantPublicId is provided, validate the new tenant.
         * Otherwise retain the existing tenant.
         */
        Tenant tenant;

        if (request.getTenantPublicId() != null
                && !request.getTenantPublicId().isBlank()) {

            tenant =
                    tenantValidator.validateAndGet(
                            request.getTenantPublicId()
                    );

        } else {

            tenant =
                    existingAgent.getTenant();
        }

        agentValidator.validateUpdate(
                request,
                tenant.getId(),
                existingAgent.getPublicId()
        );

        agentMapper.updateEntityFromRequest(
                request,
                existingAgent
        );

        /*
         * Update tenant relationship.
         */
        existingAgent.setTenant(tenant);

        /*
         * Tenant belongs to Organization.
         */
        existingAgent.setOrganization(
                tenant.getOrganization()
        );

        Agent updatedAgent =
                agentRepository.save(
                        existingAgent
                );

        log.info(
                "Agent updated successfully. Public Id : {}",
                updatedAgent.getPublicId()
        );

        return agentMapper.toResponse(
                updatedAgent
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AgentResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        return agentMapper.toResponse(agent);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AgentResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Agents. Page : {}, Size : {}",
                page,
                size
        );

        Page<Agent> agentPage =
                agentRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<AgentResponse>builder()
                .content(
                        agentPage.getContent()
                                .stream()
                                .map(agentMapper::toResponse)
                                .toList()
                )
                .pageNumber(
                        agentPage.getNumber()
                )
                .pageSize(
                        agentPage.getSize()
                )
                .totalElements(
                        agentPage.getTotalElements()
                )
                .totalPages(
                        agentPage.getTotalPages()
                )
                .first(
                        agentPage.isFirst()
                )
                .last(
                        agentPage.isLast()
                )
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        agent.markAsDeleted(1L);

        agentRepository.save(agent);

        log.info(
                "Agent deleted successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        agent.activate(1L);

        agentRepository.save(agent);

        log.info(
                "Agent activated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        agent.deactivate(1L);

        agentRepository.save(agent);

        log.info(
                "Agent deactivated successfully. Public Id : {}",
                publicId
        );
    }
}
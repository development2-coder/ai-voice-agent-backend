package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.mapper.FlowMapper;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeService;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import com.infinitio.aivoiceplatform.flow.validator.FlowGraphValidator;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of Flow management operations.
 *
 * <p>
 * This service owns Flow-level lifecycle operations.
 * Node lifecycle operations are delegated to
 * {@link FlowNodeService}.
 * </p>
 *
 * <p>
 * Graph validation is delegated to
 * {@link FlowGraphValidator} before a Flow is activated.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowServiceImpl
        implements FlowService {

    /**
     * Represents a soft-deleted Flow.
     */
    private static final Integer DELETED = 1;

    /**
     * Flow repository.
     */
    private final FlowRepository flowRepository;

    /**
     * Agent validator.
     */
    private final AgentValidator agentValidator;

    /**
     * Flow validator.
     */
    private final FlowValidator flowValidator;

    /**
     * Flow mapper.
     */
    private final FlowMapper flowMapper;

    /**
     * Node management service.
     */
    private final FlowNodeService flowNodeService;

    /**
     * Graph structure validator.
     */
    private final FlowGraphValidator flowGraphValidator;

    /**
     * Current authenticated user service.
     */
    private final CurrentUserService currentUserService;

    // =========================================================
    // CREATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowResponse create(
            CreateFlowRequest request) {

        log.info(
                "Creating Flow. agentPublicId={}, name={}",
                request.getAgentPublicId(),
                request.getName()
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        flowValidator.validateForCreate(
                request,
                agent
        );

        Flow flow =
                flowMapper.toEntity(
                        request
                );

        flow.setAgent(
                agent
        );

        flow.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        Flow saved =
                flowRepository.save(
                        flow
                );

        log.info(
                "Flow created successfully. " +
                        "publicId={}, agentPublicId={}",
                saved.getPublicId(),
                agent.getPublicId()
        );

        return flowMapper.toResponse(
                saved
        );
    }

    // =========================================================
    // UPDATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowResponse update(
            UpdateFlowRequest request) {

        log.info(
                "Updating Flow. publicId={}",
                request.getPublicId()
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        flowValidator.validateForUpdate(
                request,
                agent
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getPublicId()
                );

        flowMapper.updateEntity(
                request,
                flow
        );

        flow.setAgent(
                agent
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        Flow saved =
                flowRepository.save(
                        flow
                );

        log.info(
                "Flow updated successfully. " +
                        "publicId={}, agentPublicId={}",
                saved.getPublicId(),
                agent.getPublicId()
        );

        return flowMapper.toResponse(
                saved
        );
    }

    // =========================================================
    // GET FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FlowResponse getByPublicId(
            String publicId) {

        log.debug(
                "Fetching Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        return flowMapper.toResponse(
                flow
        );
    }

    // =========================================================
    // GET NODES
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FlowNodeResponse> getNodes(
            String flowPublicId) {

        log.debug(
                "Delegating Flow node retrieval. " +
                        "flowPublicId={}",
                flowPublicId
        );

        return flowNodeService.getNodes(
                flowPublicId
        );
    }

    // =========================================================
    // ADD NODE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeResponse addNode(
            AddFlowNodeRequest request) {

        log.info(
                "Delegating Flow node creation. " +
                        "flowPublicId={}, nodeKey={}, nodeType={}",
                request.getFlowPublicId(),
                request.getNodeKey(),
                request.getNodeType()
        );

        return flowNodeService.addNode(
                request
        );
    }

    // =========================================================
    // UPDATE NODE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeResponse updateNode(
            UpdateFlowNodeRequest request) {

        log.info(
                "Delegating Flow node update. " +
                        "nodePublicId={}",
                request.getPublicId()
        );

        return flowNodeService.updateNode(
                request
        );
    }

    // =========================================================
    // DELETE NODE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNode(
            String nodePublicId) {

        log.info(
                "Delegating Flow node deletion. " +
                        "nodePublicId={}",
                nodePublicId
        );

        flowNodeService.deleteNode(
                nodePublicId
        );
    }

    // =========================================================
    // ACTIVATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * A Flow can only become ACTIVE after the complete graph
     * has passed structural validation.
     * </p>
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        /*
         * Validate the complete graph before activation.
         *
         * This checks:
         * - START node
         * - END node
         * - edge integrity
         * - node reachability
         * - END node outgoing transitions
         */
        flowGraphValidator.validateForActivation(
                flow
        );

        flow.setStatus(
                FlowStatus.ACTIVE
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        flowRepository.save(
                flow
        );

        log.info(
                "Flow activated successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
    // DEACTIVATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        flow.setStatus(
                FlowStatus.INACTIVE
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        flowRepository.save(
                flow
        );

        log.info(
                "Flow deactivated successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
    // DELETE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        flow.setIsDeleted(
                DELETED
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        flowRepository.save(
                flow
        );

        log.info(
                "Flow deleted successfully. publicId={}",
                publicId
        );
    }
}
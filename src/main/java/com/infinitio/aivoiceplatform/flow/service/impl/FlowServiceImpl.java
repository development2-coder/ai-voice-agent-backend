package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.mapper.FlowEdgeMapper;
import com.infinitio.aivoiceplatform.flow.mapper.FlowMapper;
import com.infinitio.aivoiceplatform.flow.mapper.FlowNodeMapper;
import com.infinitio.aivoiceplatform.flow.repository.FlowEdgeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowServiceImpl
        implements FlowService {

    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    private final FlowRepository flowRepository;

    private final FlowNodeRepository nodeRepository;

    private final FlowEdgeRepository edgeRepository;

    private final AgentValidator agentValidator;

    private final FlowValidator flowValidator;

    private final FlowMapper flowMapper;

    private final FlowNodeMapper nodeMapper;

    private final FlowEdgeMapper edgeMapper;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE FLOW
    // =========================================================

    @Override
    public FlowResponse create(
            CreateFlowRequest request) {

        log.info(
                "Creating flow. agentPublicId={}, name={}",
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

        flow.setAgent(agent);

        flow.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        Flow saved =
                flowRepository.save(
                        flow
                );

        log.info(
                "Flow created successfully. publicId={}",
                saved.getPublicId()
        );

        return flowMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // UPDATE FLOW
    // =========================================================

    @Override
    public FlowResponse update(
            UpdateFlowRequest request) {

        log.info(
                "Updating flow. publicId={}",
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

        flow.setAgent(agent);

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        Flow saved =
                flowRepository.save(
                        flow
                );

        return flowMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // GET FLOW
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public FlowResponse getByPublicId(
            String publicId) {

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

    @Override
    @Transactional(readOnly = true)
    public List<FlowNodeResponse> getNodes(
            String flowPublicId) {

        Flow flow =
                flowValidator.validateAndGet(
                        flowPublicId
                );

        return nodeRepository
                .findByFlowIdAndIsDeletedOrderByIdAsc(
                        flow.getId(),
                        NOT_DELETED
                )
                .stream()
                .map(nodeMapper::toResponse)
                .toList();
    }


    // =========================================================
    // GET EDGES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<FlowEdgeResponse> getEdges(
            String flowPublicId) {

        Flow flow =
                flowValidator.validateAndGet(
                        flowPublicId
                );

        return edgeRepository
                .findByFlowIdAndIsDeletedOrderByPriorityAsc(
                        flow.getId(),
                        NOT_DELETED
                )
                .stream()
                .map(edgeMapper::toResponse)
                .toList();
    }


    // =========================================================
    // ADD NODE
    // =========================================================

    @Override
    public FlowNodeResponse addNode(
            AddFlowNodeRequest request) {

        log.info(
                "Adding node. flow={}, nodeKey={}, type={}",
                request.getFlowPublicId(),
                request.getNodeKey(),
                request.getNodeType()
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        flowValidator.validateNodeLimit(
                flow
        );

        /*
         * Validate duplicate node key inside
         * the same flow.
         */
        if (nodeRepository
                .existsByFlowIdAndNodeKeyAndIsDeleted(
                        flow.getId(),
                        request.getNodeKey(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    "Node key already exists in flow."
            );
        }

        /*
         * A flow can contain only one START node.
         */
        if (request.getNodeType()
                == FlowNodeType.START
                && nodeRepository
                .findByFlowIdAndNodeTypeAndIsDeleted(
                        flow.getId(),
                        FlowNodeType.START,
                        NOT_DELETED
                )
                .isPresent()) {

            throw new ConflictException(
                    FlowMessages.MULTIPLE_START_NODES
            );
        }

        FlowNode node =
                nodeMapper.toEntity(
                        request
                );

        node.setFlow(flow);

        /*
         * Audit information.
         *
         * created_by is mandatory in the database,
         * therefore it must be populated before save.
         */
        node.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        FlowNode saved =
                nodeRepository.save(
                        node
                );

        log.info(
                "Flow node created successfully. " +
                        "flowPublicId={}, nodePublicId={}, nodeKey={}, nodeType={}",
                flow.getPublicId(),
                saved.getPublicId(),
                saved.getNodeKey(),
                saved.getNodeType()
        );

        return nodeMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // UPDATE NODE
    // =========================================================

    @Override
    public FlowNodeResponse updateNode(
            UpdateFlowNodeRequest request) {

        log.info(
                "Updating node. publicId={}",
                request.getPublicId()
        );

        FlowNode node =
                nodeRepository
                        .findByPublicIdAndIsDeleted(
                                request.getPublicId(),
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.NODE_NOT_FOUND
                                )
                        );

        /*
         * Validate duplicate node key only when
         * the node key is actually changed.
         */
        if (!node.getNodeKey()
                .equals(request.getNodeKey())
                && nodeRepository
                .existsByFlowIdAndNodeKeyAndIsDeletedAndPublicIdNot(
                        node.getFlow().getId(),
                        request.getNodeKey(),
                        NOT_DELETED,
                        request.getPublicId()
                )) {

            throw new ConflictException(
                    "Node key already exists in flow."
            );
        }

        /*
         * START node cannot be converted to another
         * node type.
         */
        if (node.getNodeType()
                == FlowNodeType.START
                && request.getNodeType()
                != FlowNodeType.START) {

            throw new ConflictException(
                    "START node type cannot be changed."
            );
        }

        nodeMapper.updateEntity(
                request,
                node
        );

        node.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        FlowNode saved =
                nodeRepository.save(
                        node
                );

        log.info(
                "Flow node updated successfully. publicId={}",
                saved.getPublicId()
        );

        return nodeMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // DELETE NODE
    // =========================================================

    @Override
    public void deleteNode(
            String nodePublicId) {

        log.info(
                "Deleting flow node. publicId={}",
                nodePublicId
        );

        FlowNode node =
                nodeRepository
                        .findByPublicIdAndIsDeleted(
                                nodePublicId,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.NODE_NOT_FOUND
                                )
                        );

        /*
         * START node must always remain present
         * while the flow exists.
         */
        if (node.getNodeType()
                == FlowNodeType.START) {

            throw new ConflictException(
                    "START node cannot be deleted."
            );
        }

        /*
         * Soft delete.
         */
        node.setIsDeleted(
                DELETED
        );

        node.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        nodeRepository.save(
                node
        );

        log.info(
                "Flow node deleted successfully. publicId={}",
                nodePublicId
        );
    }


    // =========================================================
    // ADD EDGE
    // =========================================================

    @Override
    public FlowEdgeResponse addEdge(
            AddFlowEdgeRequest request) {

        log.info(
                "Adding edge. flow={}, source={}, target={}",
                request.getFlowPublicId(),
                request.getSourceNodeKey(),
                request.getTargetNodeKey()
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        FlowNode source =
                nodeRepository
                        .findByFlowIdAndNodeKeyAndIsDeleted(
                                flow.getId(),
                                request.getSourceNodeKey(),
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Source node not found."
                                )
                        );

        FlowNode target =
                nodeRepository
                        .findByFlowIdAndNodeKeyAndIsDeleted(
                                flow.getId(),
                                request.getTargetNodeKey(),
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Target node not found."
                                )
                        );

        if (source.getId()
                .equals(target.getId())) {

            throw new ConflictException(
                    "Source and target node cannot be same."
            );
        }

        if (edgeRepository
                .existsBySourceNodeIdAndTargetNodeIdAndIsDeleted(
                        source.getId(),
                        target.getId(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    "Flow edge already exists."
            );
        }

        FlowEdge edge =
                edgeMapper.toEntity(
                        request
                );

        edge.setFlow(flow);

        edge.setSourceNode(
                source
        );

        edge.setTargetNode(
                target
        );

        /*
         * Audit information.
         */
        edge.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        FlowEdge saved =
                edgeRepository.save(
                        edge
                );

        log.info(
                "Flow edge created successfully. " +
                        "flowPublicId={}, edgePublicId={}, source={}, target={}",
                flow.getPublicId(),
                saved.getPublicId(),
                source.getNodeKey(),
                target.getNodeKey()
        );

        return edgeMapper.toResponse(
                saved
        );
    }


    // =========================================================
    // DELETE EDGE
    // =========================================================

    @Override
    public void deleteEdge(
            String edgePublicId) {

        log.info(
                "Deleting flow edge. publicId={}",
                edgePublicId
        );

        FlowEdge edge =
                edgeRepository
                        .findByPublicIdAndIsDeleted(
                                edgePublicId,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.EDGE_NOT_FOUND
                                )
                        );

        /*
         * Soft delete.
         */
        edge.setIsDeleted(
                DELETED
        );

        edge.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        edgeRepository.save(
                edge
        );

        log.info(
                "Flow edge deleted successfully. publicId={}",
                edgePublicId
        );
    }


    // =========================================================
    // ACTIVATE FLOW
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        validateBeforeActivation(
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
    // VALIDATE BEFORE ACTIVATION
    // =========================================================

    private void validateBeforeActivation(
            Flow flow) {

        boolean startExists =
                nodeRepository
                        .findByFlowIdAndNodeTypeAndIsDeleted(
                                flow.getId(),
                                FlowNodeType.START,
                                NOT_DELETED
                        )
                        .isPresent();

        if (!startExists) {

            throw new ConflictException(
                    FlowMessages.START_NODE_REQUIRED
            );
        }

        boolean endExists =
                nodeRepository
                        .findByFlowIdAndNodeTypeAndIsDeleted(
                                flow.getId(),
                                FlowNodeType.END,
                                NOT_DELETED
                        )
                        .isPresent();

        if (!endExists) {

            throw new ConflictException(
                    FlowMessages.END_NODE_REQUIRED
            );
        }
    }


    // =========================================================
    // DEACTIVATE FLOW
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating flow. publicId={}",
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

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        /*
         * Soft delete the flow.
         */
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
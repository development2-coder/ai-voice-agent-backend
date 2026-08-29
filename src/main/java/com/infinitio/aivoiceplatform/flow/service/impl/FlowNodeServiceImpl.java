package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.mapper.FlowNodeMapper;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeService;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of Flow node management.
 *
 * <p>
 * This service owns node lifecycle operations so that the main
 * Flow service remains focused on Flow-level operations.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowNodeServiceImpl
        implements FlowNodeService {

    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    private final FlowNodeRepository nodeRepository;

    private final FlowValidator flowValidator;

    private final FlowNodeMapper nodeMapper;

    private final CurrentUserService currentUserService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FlowNodeResponse> getNodes(
            String flowPublicId) {

        log.debug(
                "Fetching Flow nodes. flowPublicId={}",
                flowPublicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        flowPublicId
                );

        List<FlowNodeResponse> nodes =
                nodeRepository
                        .findByFlowIdAndIsDeletedOrderByIdAsc(
                                flow.getId(),
                                NOT_DELETED
                        )
                        .stream()
                        .map(
                                nodeMapper::toResponse
                        )
                        .toList();

        log.debug(
                "Flow nodes fetched successfully. " +
                        "flowPublicId={}, nodeCount={}",
                flowPublicId,
                nodes.size()
        );

        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeResponse addNode(
            AddFlowNodeRequest request) {

        log.info(
                "Adding Flow node. " +
                        "flowPublicId={}, nodeKey={}, nodeType={}",
                request.getFlowPublicId(),
                request.getNodeKey(),
                request.getNodeType()
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        /*
         * Check configured maximum number of nodes.
         */
        flowValidator.validateNodeLimit(
                flow
        );

        /*
         * Node key must be unique inside a Flow.
         */
        validateDuplicateNodeKey(
                flow,
                request.getNodeKey()
        );

        /*
         * A Flow may contain only one START node.
         */
        validateStartNode(
                flow,
                request.getNodeType()
        );

        FlowNode node =
                nodeMapper.toEntity(
                        request
                );

        node.setFlow(
                flow
        );

        node.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        FlowNode saved =
                nodeRepository.save(
                        node
                );

        log.info(
                "Flow node created successfully. " +
                        "flowPublicId={}, nodePublicId={}, " +
                        "nodeKey={}, nodeType={}",
                flow.getPublicId(),
                saved.getPublicId(),
                saved.getNodeKey(),
                saved.getNodeType()
        );

        return nodeMapper.toResponse(
                saved
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeResponse updateNode(
            UpdateFlowNodeRequest request) {

        log.info(
                "Updating Flow node. nodePublicId={}",
                request.getPublicId()
        );

        FlowNode node =
                findActiveNode(
                        request.getPublicId()
                );

        validateNodeKeyChange(
                node,
                request
        );

        validateNodeTypeChange(
                node,
                request
        );

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
                "Flow node updated successfully. " +
                        "nodePublicId={}, nodeKey={}, nodeType={}",
                saved.getPublicId(),
                saved.getNodeKey(),
                saved.getNodeType()
        );

        return nodeMapper.toResponse(
                saved
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNode(
            String nodePublicId) {

        log.info(
                "Deleting Flow node. nodePublicId={}",
                nodePublicId
        );

        FlowNode node =
                findActiveNode(
                        nodePublicId
                );

        validateNodeDeletion(
                node
        );

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
                "Flow node deleted successfully. " +
                        "nodePublicId={}, nodeKey={}",
                nodePublicId,
                node.getNodeKey()
        );
    }

    /**
     * Validates node-key uniqueness.
     *
     * @param flow Flow
     * @param nodeKey node key
     */
    private void validateDuplicateNodeKey(
            Flow flow,
            String nodeKey) {

        boolean exists =
                nodeRepository
                        .existsByFlowIdAndNodeKeyAndIsDeleted(
                                flow.getId(),
                                nodeKey,
                                NOT_DELETED
                        );

        if (!exists) {
            return;
        }

        log.warn(
                "Duplicate Flow node key rejected. " +
                        "flowPublicId={}, nodeKey={}",
                flow.getPublicId(),
                nodeKey
        );

        throw new ConflictException(
                FlowMessages.NODE_KEY_ALREADY_EXISTS
        );
    }

    /**
     * Validates START node creation.
     *
     * @param flow Flow
     * @param nodeType requested node type
     */
    private void validateStartNode(
            Flow flow,
            FlowNodeType nodeType) {

        if (nodeType != FlowNodeType.START) {
            return;
        }

        boolean startExists =
                nodeRepository
                        .findByFlowIdAndNodeTypeAndIsDeleted(
                                flow.getId(),
                                FlowNodeType.START,
                                NOT_DELETED
                        )
                        .isPresent();

        if (!startExists) {
            return;
        }

        log.warn(
                "Multiple START nodes rejected. " +
                        "flowPublicId={}",
                flow.getPublicId()
        );

        throw new ConflictException(
                FlowMessages.MULTIPLE_START_NODES
        );
    }

    /**
     * Finds an active node.
     *
     * @param nodePublicId node public identifier
     * @return active node
     */
    private FlowNode findActiveNode(
            String nodePublicId) {

        return nodeRepository
                .findByPublicIdAndIsDeleted(
                        nodePublicId,
                        NOT_DELETED
                )
                .orElseThrow(() -> {

                    log.warn(
                            "Flow node not found. " +
                                    "nodePublicId={}",
                            nodePublicId
                    );

                    return new ResourceNotFoundException(
                            FlowMessages.NODE_NOT_FOUND
                    );
                });
    }

    /**
     * Validates node key update.
     *
     * @param node existing node
     * @param request update request
     */
    private void validateNodeKeyChange(
            FlowNode node,
            UpdateFlowNodeRequest request) {

        if (node.getNodeKey()
                .equals(request.getNodeKey())) {

            return;
        }

        boolean exists =
                nodeRepository
                        .existsByFlowIdAndNodeKeyAndIsDeletedAndPublicIdNot(
                                node.getFlow().getId(),
                                request.getNodeKey(),
                                NOT_DELETED,
                                request.getPublicId()
                        );

        if (!exists) {
            return;
        }

        log.warn(
                "Duplicate Flow node key during update. " +
                        "flowId={}, nodePublicId={}, nodeKey={}",
                node.getFlow().getId(),
                request.getPublicId(),
                request.getNodeKey()
        );

        throw new ConflictException(
                FlowMessages.NODE_KEY_ALREADY_EXISTS
        );
    }

    /**
     * Prevents START from being changed to another type.
     *
     * @param node existing node
     * @param request update request
     */
    private void validateNodeTypeChange(
            FlowNode node,
            UpdateFlowNodeRequest request) {

        if (node.getNodeType()
                != FlowNodeType.START) {

            return;
        }

        if (request.getNodeType()
                == FlowNodeType.START) {

            return;
        }

        log.warn(
                "START node type change rejected. " +
                        "nodePublicId={}, requestedType={}",
                node.getPublicId(),
                request.getNodeType()
        );

        throw new ConflictException(
                FlowMessages.START_NODE_TYPE_CANNOT_BE_CHANGED
        );
    }

    /**
     * Prevents deletion of START.
     *
     * @param node node
     */
    private void validateNodeDeletion(
            FlowNode node) {

        if (node.getNodeType()
                != FlowNodeType.START) {

            return;
        }

        log.warn(
                "START node deletion rejected. nodePublicId={}",
                node.getPublicId()
        );

        throw new ConflictException(
                FlowMessages.START_NODE_CANNOT_BE_DELETED
        );
    }
}
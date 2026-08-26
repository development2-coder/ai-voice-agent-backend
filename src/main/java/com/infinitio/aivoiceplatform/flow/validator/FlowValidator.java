package com.infinitio.aivoiceplatform.flow.validator;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlowValidator {

    private static final Integer NOT_DELETED = 0;

    private final FlowRepository flowRepository;

    private final FlowNodeRepository flowNodeRepository;


    // =========================================================
    // VALIDATE AND GET
    // =========================================================

    public Flow validateAndGet(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "Flow public ID is required."
            );
        }

        return flowRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                FlowMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateFlowRequest request,
            Agent agent) {

        if (request == null) {

            throw new BadRequestException(
                    "Flow request cannot be null."
            );
        }

        if (agent == null) {

            throw new BadRequestException(
                    "Agent is required."
            );
        }

        if (flowRepository
                .existsByAgentIdAndNameAndIsDeleted(
                        agent.getId(),
                        request.getName().trim(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    "Flow name already exists for this agent."
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateFlowRequest request,
            Agent agent) {

        if (request == null) {

            throw new BadRequestException(
                    "Flow update request cannot be null."
            );
        }

        if (agent == null) {

            throw new BadRequestException(
                    "Agent is required."
            );
        }

        Flow existing =
                validateAndGet(
                        request.getPublicId()
                );

        if (flowRepository
                .existsByAgentIdAndNameAndIsDeletedAndPublicIdNot(
                        agent.getId(),
                        request.getName().trim(),
                        NOT_DELETED,
                        request.getPublicId()
                )) {

            throw new ConflictException(
                    "Flow name already exists for this agent."
            );
        }
    }


    // =========================================================
    // NODE VALIDATION
    // =========================================================

    public void validateStartNode(
            FlowNode node) {

        if (node == null
                || node.getNodeType()
                != FlowNodeType.START) {

            throw new ConflictException(
                    "Invalid start node."
            );
        }
    }


    // =========================================================
    // MAX NODE VALIDATION
    // =========================================================

    public void validateNodeLimit(
            Flow flow) {

        long nodeCount =
                flowNodeRepository
                        .findByFlowIdAndIsDeletedOrderByIdAsc(
                                flow.getId(),
                                NOT_DELETED
                        )
                        .size();

        if (nodeCount >=
                com.infinitio.aivoiceplatform.flow.constant.FlowConstants.MAX_FLOW_NODES) {

            throw new ConflictException(
                    "Maximum number of flow nodes reached."
            );
        }
    }
}
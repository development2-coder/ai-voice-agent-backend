package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FlowResultServiceImpl
        implements FlowResultService {

    @Override
    public FlowExecutionResult buildResult(
            FlowExecution execution,
            FlowNode node,
            FlowNodeExecutionResult nodeResult) {

        if (execution == null) {
            throw new IllegalArgumentException(
                    "Flow execution cannot be null."
            );
        }

        if (node == null) {
            throw new IllegalArgumentException(
                    "Flow node cannot be null."
            );
        }

        if (nodeResult == null) {
            throw new IllegalArgumentException(
                    "Flow node execution result cannot be null."
            );
        }

        FlowExecutionStatus status =
                execution.getStatus();

        boolean waitingForInput =
                status == FlowExecutionStatus.WAITING_FOR_INPUT;

        boolean waitingForAi =
                status == FlowExecutionStatus.WAITING_FOR_AI;

        boolean waitingForApi =
                status == FlowExecutionStatus.WAITING_FOR_API;

        boolean completed =
                status == FlowExecutionStatus.COMPLETED
                        || nodeResult.isCompleted();

        boolean transferred =
                status == FlowExecutionStatus.TRANSFERRED
                        || nodeResult.isTransferred();

        return FlowExecutionResult.builder()
                .executionPublicId(
                        execution.getPublicId()
                )
                .status(
                        status
                )
                .currentNodeKey(
                        node.getNodeKey()
                )
                .currentNodeType(
                        node.getNodeType().name()
                )
                .outputText(
                        nodeResult.getOutputText()
                )
                .action(
                        nodeResult.getAction()
                )
                .waitingForInput(
                        waitingForInput
                )
                .waitingForAi(
                        waitingForAi
                )
                .waitingForApi(
                        waitingForApi
                )
                .transferred(
                        transferred
                )
                .completed(
                        completed
                )
                .context(
                        nodeResult.getContext()
                )
                .build();
    }
}
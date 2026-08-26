package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ConditionNodeHandler
        implements FlowNodeHandler {

    @Override
    public FlowNodeType getNodeType() {
        return FlowNodeType.CONDITION;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing condition node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        /*
         * Condition evaluation is NOT performed here.
         *
         * FlowTransitionService is responsible for:
         *
         * 1. Getting outgoing edges
         * 2. Reading edge conditions
         * 3. Evaluating conditions
         * 4. Selecting the matching edge
         * 5. Returning the next node
         */

        return FlowNodeExecutionResult.builder()
                .action("EVALUATE_CONDITION")
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }
}
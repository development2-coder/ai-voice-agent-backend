package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GreetingNodeHandler
        implements FlowNodeHandler {

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.GREETING;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing GREETING node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        String greeting =
                "Hello! How can I help you today?";

        /*
         * Resolve any variables that may be present
         * in the greeting.
         *
         * Example:
         *
         * Hello {{customerName}}
         */
        String resolvedGreeting =
                flowContextService.replaceVariables(
                        greeting,
                        context
                );

        return FlowNodeExecutionResult.builder()
                .action("SPEAK")
                .outputText(
                        resolvedGreeting
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }
}
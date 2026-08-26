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
public class StartNodeHandler
        implements FlowNodeHandler {

    @Override
    public FlowNodeType getNodeType() {
        return FlowNodeType.START;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.debug(
                "Executing START node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        return FlowNodeExecutionResult.builder()
                .action("CONTINUE")
                .context(context)
                .build();
    }
}
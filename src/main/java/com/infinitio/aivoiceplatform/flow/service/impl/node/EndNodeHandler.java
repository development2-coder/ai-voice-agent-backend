package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class EndNodeHandler
        implements FlowNodeHandler {

    @Override
    public FlowNodeType getNodeType() {
        return FlowNodeType.END;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        execution.setStatus(
                FlowExecutionStatus.COMPLETED
        );

        execution.setCompletedAt(
                LocalDateTime.now()
        );

        log.info(
                "Flow execution completed. execution={}",
                execution.getPublicId()
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.COMPLETED
                )
                .action("END")
                .completed(true)
                .context(context)
                .build();
    }
}
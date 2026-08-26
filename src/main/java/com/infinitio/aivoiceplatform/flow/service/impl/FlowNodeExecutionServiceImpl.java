package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeExecutionService;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.flow.service.impl.node.FlowNodeHandler;
import com.infinitio.aivoiceplatform.flow.service.impl.node.FlowNodeHandlerFactory;
import com.infinitio.aivoiceplatform.flow.repository.FlowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowNodeExecutionServiceImpl
        implements FlowNodeExecutionService {

    private final FlowNodeHandlerFactory handlerFactory;

    private final FlowExecutionRepository executionRepository;

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeExecutionResult execute(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing flow node. execution={}, node={}, type={}",
                execution.getPublicId(),
                node.getNodeKey(),
                node.getNodeType()
        );

        /*
         * Update current node before execution.
         */
        execution.setCurrentNodeId(
                node.getId()
        );

        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        executionRepository.save(
                execution
        );

        /*
         * Find the handler responsible for
         * this node type.
         */
        FlowNodeHandler handler =
                handlerFactory.getHandler(
                        node.getNodeType()
                );

        log.debug(
                "Flow node handler found. node={}, handler={}",
                node.getNodeKey(),
                handler.getClass().getSimpleName()
        );

        /*
         * Execute exactly one node.
         */
        FlowNodeExecutionResult result =
                handler.handle(
                        execution,
                        node,
                        context
                );

        /*
         * Save any changes made by the handler.
         */
        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        if (result.getStatus() != null) {

            execution.setStatus(
                    result.getStatus()
            );
        }

        executionRepository.save(
                execution
        );

        log.info(
                "Flow node execution completed. execution={}, node={}, action={}, status={}",
                execution.getPublicId(),
                node.getNodeKey(),
                result.getAction(),
                result.getStatus()
        );

        return result;
    }
}
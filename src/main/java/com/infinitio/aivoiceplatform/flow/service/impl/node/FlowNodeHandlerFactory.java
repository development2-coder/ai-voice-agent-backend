package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FlowNodeHandlerFactory {

    private final Map<FlowNodeType, FlowNodeHandler> handlers =
            new EnumMap<>(FlowNodeType.class);

    public FlowNodeHandlerFactory(
            List<FlowNodeHandler> handlerList) {

        for (FlowNodeHandler handler : handlerList) {

            handlers.put(
                    handler.getNodeType(),
                    handler
            );
        }

        log.info(
                "Registered {} flow node handlers.",
                handlers.size()
        );
    }

    public FlowNodeHandler getHandler(
            FlowNodeType nodeType) {

        FlowNodeHandler handler =
                handlers.get(nodeType);

        if (handler == null) {

            throw new ResourceNotFoundException(
                    "No handler registered for node type: "
                            + nodeType
            );
        }

        return handler;
    }
}
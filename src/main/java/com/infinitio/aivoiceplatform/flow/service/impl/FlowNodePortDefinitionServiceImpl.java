package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodePortType;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodePortResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowNodePortDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides input and output port definitions for built-in
 * Flow node types.
 *
 * <p>
 * The port model is inspired by the n8n workflow model where
 * nodes expose connection points and branching nodes can expose
 * multiple outputs.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class FlowNodePortDefinitionServiceImpl
        implements FlowNodePortDefinitionService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FlowNodePortResponse> getInputPorts(
            FlowNodeType nodeType) {

        if (nodeType == null) {
            return List.of();
        }

        log.debug(
                "Resolving input ports. nodeType={}",
                nodeType
        );

        return switch (nodeType) {

            case START ->
                    List.of();

            case END,
                 GREETING,
                 MESSAGE,
                 USER_INPUT,
                 AI_RESPONSE,
                 CONDITION,
                 API,
                 WEBHOOK,
                 FUNCTION,
                 KNOWLEDGE_BASE,
                 RAG,
                 SET_VARIABLE,
                 TRANSFER,
                 WAIT,
                 STT,
                 LLM,
                 TTS ->
                    List.of(
                            createPort(
                                    "main",
                                    "Main",
                                    FlowNodePortType.INPUT,
                                    "ANY",
                                    true,
                                    true
                            )
                    );
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FlowNodePortResponse> getOutputPorts(
            FlowNodeType nodeType) {

        if (nodeType == null) {
            return List.of();
        }

        log.debug(
                "Resolving output ports. nodeType={}",
                nodeType
        );

        return switch (nodeType) {

            case END ->
                    List.of();

            case CONDITION ->
                    List.of(
                            createPort(
                                    "true",
                                    "True",
                                    FlowNodePortType.OUTPUT,
                                    "ANY",
                                    false,
                                    true
                            ),
                            createPort(
                                    "false",
                                    "False",
                                    FlowNodePortType.OUTPUT,
                                    "ANY",
                                    false,
                                    true
                            )
                    );

            case START,
                 GREETING,
                 MESSAGE,
                 USER_INPUT,
                 AI_RESPONSE,
                 API,
                 WEBHOOK,
                 FUNCTION,
                 KNOWLEDGE_BASE,
                 RAG,
                 SET_VARIABLE,
                 TRANSFER,
                 WAIT,
                 STT,
                 LLM,
                 TTS ->
                    List.of(
                            createPort(
                                    "main",
                                    "Main",
                                    FlowNodePortType.OUTPUT,
                                    "ANY",
                                    false,
                                    true
                            )
                    );
        };
    }

    /**
     * Creates a port definition.
     *
     * @param portId port identifier
     * @param displayName display name
     * @param type port direction
     * @param dataType port data type
     * @param required whether required
     * @param multipleConnections whether multiple connections are allowed
     * @return port definition
     */
    private FlowNodePortResponse createPort(
            String portId,
            String displayName,
            FlowNodePortType type,
            String dataType,
            boolean required,
            boolean multipleConnections) {

        return FlowNodePortResponse
                .builder()
                .portId(portId)
                .displayName(displayName)
                .type(type)
                .dataType(dataType)
                .required(required)
                .multipleConnections(multipleConnections)
                .build();
    }
}
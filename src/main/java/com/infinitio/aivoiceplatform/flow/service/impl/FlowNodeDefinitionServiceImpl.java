package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeConfigurationSchemaService;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeDefinitionService;
import com.infinitio.aivoiceplatform.flow.service.FlowNodePortDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Provides metadata for the built-in Flow node library.
 *
 * <p>
 * This service is responsible only for node metadata. Configuration
 * schemas and connection-port definitions are delegated to their
 * respective services to keep the implementation modular.
 * </p>
 *
 * <p>
 * The architecture is inspired by n8n's node-oriented workflow
 * model, where the workflow canvas can discover node capabilities
 * and configuration rather than hardcoding every node in the UI.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowNodeDefinitionServiceImpl
        implements FlowNodeDefinitionService {

    private final FlowNodeConfigurationSchemaService
            configurationSchemaService;

    private final FlowNodePortDefinitionService
            portDefinitionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FlowNodeDefinitionResponse> getAll() {

        log.info(
                "Fetching all Flow node definitions."
        );

        return Arrays.stream(
                        FlowNodeType.values()
                )
                .map(
                        this::buildDefinition
                )
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeDefinitionResponse getByType(
            FlowNodeType nodeType) {

        if (nodeType == null) {

            log.warn(
                    "Flow node definition requested without node type."
            );

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        log.info(
                "Fetching Flow node definition. nodeType={}",
                nodeType
        );

        return buildDefinition(
                nodeType
        );
    }

    /**
     * Builds metadata for a single node type.
     *
     * @param nodeType node type
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildDefinition(
            FlowNodeType nodeType) {

        return switch (nodeType) {

            case START ->
                    buildStartDefinition();

            case GREETING ->
                    buildGreetingDefinition();

            case MESSAGE ->
                    buildMessageDefinition();

            case USER_INPUT ->
                    buildUserInputDefinition();

            case AI_RESPONSE ->
                    buildAiResponseDefinition();

            case CONDITION ->
                    buildConditionDefinition();

            case API ->
                    buildApiDefinition();

            case WEBHOOK ->
                    buildWebhookDefinition();

            case FUNCTION ->
                    buildFunctionDefinition();

            case KNOWLEDGE_BASE ->
                    buildKnowledgeBaseDefinition();

            case RAG ->
                    buildRagDefinition();

            case SET_VARIABLE ->
                    buildSetVariableDefinition();

            case TRANSFER ->
                    buildTransferDefinition();

            case WAIT ->
                    buildWaitDefinition();

            case END ->
                    buildEndDefinition();

            case STT ->
                    buildSttDefinition();

            case LLM ->
                    buildLlmDefinition();

            case TTS ->
                    buildTtsDefinition();
        };
    }

    /**
     * Builds START node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildStartDefinition() {

        return build(
                FlowNodeType.START,
                "Start",
                "TRIGGER",
                "Entry point of the voice agent flow.",
                "play",
                false,
                true,
                false,
                false,
                true,
                false
        );
    }

    /**
     * Builds GREETING node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildGreetingDefinition() {

        return build(
                FlowNodeType.GREETING,
                "Greeting",
                "CONVERSATION",
                "Starts the conversation with a greeting.",
                "hand",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds MESSAGE node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildMessageDefinition() {

        return build(
                FlowNodeType.MESSAGE,
                "Message",
                "CONVERSATION",
                "Speaks a fixed message to the caller.",
                "message",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds USER_INPUT node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildUserInputDefinition() {

        return build(
                FlowNodeType.USER_INPUT,
                "User Input",
                "INPUT",
                "Collects information from the caller.",
                "keyboard",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds AI_RESPONSE node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildAiResponseDefinition() {

        return build(
                FlowNodeType.AI_RESPONSE,
                "AI Response",
                "AI",
                "Generates a response using the configured AI model.",
                "sparkles",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds CONDITION node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildConditionDefinition() {

        return build(
                FlowNodeType.CONDITION,
                "Condition",
                "LOGIC",
                "Routes execution based on a condition.",
                "git-branch",
                true,
                false,
                false,
                true,
                true,
                true
        );
    }

    /**
     * Builds API node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildApiDefinition() {

        return build(
                FlowNodeType.API,
                "API Request",
                "INTEGRATION",
                "Calls an external HTTP API.",
                "globe",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds WEBHOOK node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildWebhookDefinition() {

        return build(
                FlowNodeType.WEBHOOK,
                "Webhook",
                "INTEGRATION",
                "Receives or sends webhook data.",
                "webhook",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds FUNCTION node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildFunctionDefinition() {

        return build(
                FlowNodeType.FUNCTION,
                "Function",
                "LOGIC",
                "Runs custom flow transformation logic.",
                "code",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds KNOWLEDGE_BASE node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse
    buildKnowledgeBaseDefinition() {

        return build(
                FlowNodeType.KNOWLEDGE_BASE,
                "Knowledge Base",
                "AI",
                "Retrieves information from a knowledge base.",
                "book-open",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds RAG node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildRagDefinition() {

        return build(
                FlowNodeType.RAG,
                "RAG",
                "AI",
                "Retrieves relevant knowledge for AI generation.",
                "database",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds SET_VARIABLE node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse
    buildSetVariableDefinition() {

        return build(
                FlowNodeType.SET_VARIABLE,
                "Set Variable",
                "DATA",
                "Creates or updates a flow variable.",
                "variable",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds TRANSFER node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildTransferDefinition() {

        return build(
                FlowNodeType.TRANSFER,
                "Transfer",
                "VOICE",
                "Transfers the current call to a destination.",
                "phone-forwarded",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds WAIT node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildWaitDefinition() {

        return build(
                FlowNodeType.WAIT,
                "Wait",
                "CONTROL",
                "Pauses flow execution before continuing.",
                "clock",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds END node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildEndDefinition() {

        return build(
                FlowNodeType.END,
                "End",
                "CONTROL",
                "Terminates the flow.",
                "square",
                false,
                false,
                true,
                true,
                false,
                false
        );
    }

    /**
     * Builds STT node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildSttDefinition() {

        return build(
                FlowNodeType.STT,
                "Speech to Text",
                "VOICE",
                "Converts caller speech into text for downstream flow processing.",
                "mic",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds LLM node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildLlmDefinition() {

        return build(
                FlowNodeType.LLM,
                "LLM",
                "AI",
                "Generates an AI response using the configured language model.",
                "sparkles",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Builds TTS node metadata.
     *
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildTtsDefinition() {

        return build(
                FlowNodeType.TTS,
                "Text to Speech",
                "VOICE",
                "Converts generated text into speech for the caller.",
                "volume-2",
                true,
                false,
                false,
                true,
                true,
                false
        );
    }

    /**
     * Creates a complete node definition.
     *
     * @param nodeType node type
     * @param displayName display name
     * @param category category
     * @param description description
     * @param icon icon identifier
     * @param userCreatable user-creatable flag
     * @param startNode start-node flag
     * @param endNode end-node flag
     * @param inputSupported input support flag
     * @param outputSupported output support flag
     * @param multipleOutputs multiple output flag
     * @return node definition
     */
    private FlowNodeDefinitionResponse build(
            FlowNodeType nodeType,
            String displayName,
            String category,
            String description,
            String icon,
            boolean userCreatable,
            boolean startNode,
            boolean endNode,
            boolean inputSupported,
            boolean outputSupported,
            boolean multipleOutputs) {

        return FlowNodeDefinitionResponse
                .builder()
                .nodeType(nodeType)
                .displayName(displayName)
                .category(category)
                .description(description)
                .icon(icon)
                .userCreatable(userCreatable)
                .startNode(startNode)
                .endNode(endNode)
                .inputSupported(inputSupported)
                .outputSupported(outputSupported)
                .multipleOutputs(multipleOutputs)
                .configurationSchema(
                        configurationSchemaService
                                .getSchema(nodeType)
                )
                .inputPorts(
                        portDefinitionService
                                .getInputPorts(nodeType)
                )
                .outputPorts(
                        portDefinitionService
                                .getOutputPorts(nodeType)
                )
                .build();
    }
}
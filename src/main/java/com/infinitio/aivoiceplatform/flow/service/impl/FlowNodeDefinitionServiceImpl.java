package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Provides the built-in node definitions available to the
 * Flow Builder.
 *
 * <p>
 * The definitions are intentionally centralized so the frontend
 * can build its node palette dynamically instead of hardcoding
 * node types.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class FlowNodeDefinitionServiceImpl
        implements FlowNodeDefinitionService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FlowNodeDefinitionResponse> getAll() {

        log.debug(
                "Fetching Flow node definitions."
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

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        log.debug(
                "Fetching Flow node definition. nodeType={}",
                nodeType
        );

        return buildDefinition(
                nodeType
        );
    }

    /**
     * Builds metadata for one node type.
     *
     * @param nodeType node type
     * @return node definition
     */
    private FlowNodeDefinitionResponse buildDefinition(
            FlowNodeType nodeType) {

        return switch (nodeType) {

            case START -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Start")
                    .category("TRIGGER")
                    .description(
                            "Entry point of the voice agent flow."
                    )
                    .icon("play")
                    .userCreatable(false)
                    .startNode(true)
                    .endNode(false)
                    .inputSupported(false)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            "{}"
                    )
                    .build();

            case GREETING -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Greeting")
                    .category("CONVERSATION")
                    .description(
                            "Starts the conversation with a greeting."
                    )
                    .icon("hand")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            greetingSchema()
                    )
                    .build();

            case MESSAGE -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Message")
                    .category("CONVERSATION")
                    .description(
                            "Speaks a fixed message to the caller."
                    )
                    .icon("message")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            messageSchema()
                    )
                    .build();

            case USER_INPUT -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("User Input")
                    .category("INPUT")
                    .description(
                            "Collects information from the caller."
                    )
                    .icon("keyboard")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            userInputSchema()
                    )
                    .build();

            case AI_RESPONSE -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("AI Response")
                    .category("AI")
                    .description(
                            "Generates a response using the configured AI model."
                    )
                    .icon("sparkles")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            aiResponseSchema()
                    )
                    .build();

            case CONDITION -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Condition")
                    .category("LOGIC")
                    .description(
                            "Routes execution based on a condition."
                    )
                    .icon("git-branch")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(true)
                    .configurationSchema(
                            conditionSchema()
                    )
                    .build();

            case API -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("API Request")
                    .category("INTEGRATION")
                    .description(
                            "Calls an external HTTP API."
                    )
                    .icon("globe")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            apiSchema()
                    )
                    .build();

            case WEBHOOK -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Webhook")
                    .category("INTEGRATION")
                    .description(
                            "Receives or sends webhook data."
                    )
                    .icon("webhook")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            webhookSchema()
                    )
                    .build();

            case FUNCTION -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Function")
                    .category("LOGIC")
                    .description(
                            "Runs custom flow transformation logic."
                    )
                    .icon("code")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            functionSchema()
                    )
                    .build();

            case KNOWLEDGE_BASE -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Knowledge Base")
                    .category("AI")
                    .description(
                            "Retrieves information from a knowledge base."
                    )
                    .icon("book-open")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            knowledgeBaseSchema()
                    )
                    .build();

            case RAG -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("RAG")
                    .category("AI")
                    .description(
                            "Retrieves relevant knowledge for AI generation."
                    )
                    .icon("database")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            ragSchema()
                    )
                    .build();

            case SET_VARIABLE -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Set Variable")
                    .category("DATA")
                    .description(
                            "Creates or updates a flow variable."
                    )
                    .icon("variable")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            setVariableSchema()
                    )
                    .build();

            case TRANSFER -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Transfer")
                    .category("VOICE")
                    .description(
                            "Transfers the current call to a destination."
                    )
                    .icon("phone-forwarded")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            transferSchema()
                    )
                    .build();

            case WAIT -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("Wait")
                    .category("CONTROL")
                    .description(
                            "Pauses flow execution before continuing."
                    )
                    .icon("clock")
                    .userCreatable(true)
                    .startNode(false)
                    .endNode(false)
                    .inputSupported(true)
                    .outputSupported(true)
                    .multipleOutputs(false)
                    .configurationSchema(
                            waitSchema()
                    )
                    .build();

            case END -> FlowNodeDefinitionResponse
                    .builder()
                    .nodeType(nodeType)
                    .displayName("End")
                    .category("CONTROL")
                    .description(
                            "Terminates the flow."
                    )
                    .icon("square")
                    .userCreatable(false)
                    .startNode(false)
                    .endNode(true)
                    .inputSupported(true)
                    .outputSupported(false)
                    .multipleOutputs(false)
                    .configurationSchema(
                            "{}"
                    )
                    .build();
        };
    }

    private String greetingSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "message",
                      "label": "Greeting",
                      "type": "TEXTAREA",
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String messageSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "message",
                      "label": "Message",
                      "type": "TEXTAREA",
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String userInputSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "variableName",
                      "label": "Variable Name",
                      "type": "TEXT",
                      "required": true
                    },
                    {
                      "name": "inputType",
                      "label": "Input Type",
                      "type": "SELECT",
                      "options": [
                        "TEXT",
                        "NUMBER",
                        "DATE",
                        "DTMF"
                      ],
                      "required": true
                    },
                    {
                      "name": "prompt",
                      "label": "Prompt",
                      "type": "TEXTAREA",
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String aiResponseSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "prompt",
                      "label": "Prompt",
                      "type": "TEXTAREA",
                      "required": true,
                      "supportsExpression": true
                    },
                    {
                      "name": "llmConfigPublicId",
                      "label": "LLM Configuration",
                      "type": "SELECT",
                      "required": true
                    },
                    {
                      "name": "temperature",
                      "label": "Temperature",
                      "type": "NUMBER",
                      "required": false
                    },
                    {
                      "name": "outputVariable",
                      "label": "Output Variable",
                      "type": "TEXT",
                      "required": false
                    }
                  ]
                }
                """;
    }

    private String conditionSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "variable",
                      "label": "Variable",
                      "type": "TEXT",
                      "required": true,
                      "supportsExpression": true
                    },
                    {
                      "name": "operator",
                      "label": "Operator",
                      "type": "SELECT",
                      "options": [
                        "EQUALS",
                        "NOT_EQUALS",
                        "GREATER_THAN",
                        "LESS_THAN",
                        "GREATER_THAN_OR_EQUALS",
                        "LESS_THAN_OR_EQUALS",
                        "CONTAINS",
                        "EXISTS",
                        "NOT_EXISTS"
                      ],
                      "required": true
                    },
                    {
                      "name": "value",
                      "label": "Value",
                      "type": "TEXT",
                      "required": true,
                      "supportsExpression": true
                    }
                  ]
                }
                """;
    }

    private String apiSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "method",
                      "label": "HTTP Method",
                      "type": "SELECT",
                      "options": [
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE"
                      ],
                      "required": true
                    },
                    {
                      "name": "url",
                      "label": "URL",
                      "type": "TEXT",
                      "required": true,
                      "supportsExpression": true
                    },
                    {
                      "name": "headers",
                      "label": "Headers",
                      "type": "JSON",
                      "required": false
                    },
                    {
                      "name": "body",
                      "label": "Request Body",
                      "type": "JSON",
                      "required": false,
                      "supportsExpression": true
                    },
                    {
                      "name": "outputVariable",
                      "label": "Output Variable",
                      "type": "TEXT",
                      "required": false
                    }
                  ]
                }
                """;
    }

    private String webhookSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "url",
                      "label": "Webhook URL",
                      "type": "TEXT",
                      "required": true
                    },
                    {
                      "name": "method",
                      "label": "HTTP Method",
                      "type": "SELECT",
                      "options": [
                        "GET",
                        "POST"
                      ],
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String functionSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "code",
                      "label": "Function Code",
                      "type": "CODE",
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String knowledgeBaseSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "knowledgeBasePublicId",
                      "label": "Knowledge Base",
                      "type": "SELECT",
                      "required": true
                    },
                    {
                      "name": "query",
                      "label": "Query",
                      "type": "TEXTAREA",
                      "required": true,
                      "supportsExpression": true
                    },
                    {
                      "name": "outputVariable",
                      "label": "Output Variable",
                      "type": "TEXT",
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String ragSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "knowledgeBasePublicId",
                      "label": "Knowledge Base",
                      "type": "SELECT",
                      "required": true
                    },
                    {
                      "name": "query",
                      "label": "Query",
                      "type": "TEXTAREA",
                      "required": true,
                      "supportsExpression": true
                    },
                    {
                      "name": "topK",
                      "label": "Top K",
                      "type": "NUMBER",
                      "required": false
                    },
                    {
                      "name": "outputVariable",
                      "label": "Output Variable",
                      "type": "TEXT",
                      "required": true
                    }
                  ]
                }
                """;
    }

    private String setVariableSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "variableName",
                      "label": "Variable Name",
                      "type": "TEXT",
                      "required": true
                    },
                    {
                      "name": "value",
                      "label": "Value",
                      "type": "TEXT",
                      "required": true,
                      "supportsExpression": true
                    }
                  ]
                }
                """;
    }

    private String transferSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "destination",
                      "label": "Transfer Destination",
                      "type": "TEXT",
                      "required": true,
                      "supportsExpression": true
                    }
                  ]
                }
                """;
    }

    private String waitSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "durationSeconds",
                      "label": "Duration",
                      "type": "NUMBER",
                      "required": true
                    }
                  ]
                }
                """;
    }
}
package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeConfigurationSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Provides configuration schemas for built-in Flow nodes.
 *
 * <p>
 * The schema format is intentionally simple and frontend-friendly.
 * It allows the visual builder to render configuration fields
 * without hardcoding a separate form for every node type.
 * </p>
 *
 * <p>
 * Voice nodes such as STT, LLM and TTS expose only configuration
 * values that are currently supported by their corresponding
 * runtime handlers.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class FlowNodeConfigurationSchemaServiceImpl
        implements FlowNodeConfigurationSchemaService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchema(
            FlowNodeType nodeType) {

        if (nodeType == null) {
            return emptySchema();
        }

        log.debug(
                "Resolving node configuration schema. nodeType={}",
                nodeType
        );

        return switch (nodeType) {

            case START, END ->
                    emptySchema();

            case GREETING ->
                    messageSchema();

            case MESSAGE ->
                    messageSchema();

            case USER_INPUT ->
                    userInputSchema();

            case AI_RESPONSE ->
                    aiResponseSchema();

            case CONDITION ->
                    conditionSchema();

            case API ->
                    apiSchema();

            case WEBHOOK ->
                    webhookSchema();

            case FUNCTION ->
                    functionSchema();

            case KNOWLEDGE_BASE ->
                    knowledgeBaseSchema();

            case RAG ->
                    ragSchema();

            case SET_VARIABLE ->
                    setVariableSchema();

            case TRANSFER ->
                    transferSchema();

            case WAIT ->
                    waitSchema();

            case STT ->
                    sttSchema();

            case LLM ->
                    llmSchema();

            case TTS ->
                    ttsSchema();
        };
    }

    // =========================================================
    // EMPTY
    // =========================================================

    /**
     * Returns an empty configuration schema.
     *
     * @return empty JSON schema
     */
    private String emptySchema() {

        return """
                {
                  "fields": []
                }
                """;
    }

    // =========================================================
    // MESSAGE
    // =========================================================

    /**
     * Returns message configuration schema.
     *
     * @return JSON schema
     */
    private String messageSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "message",
                      "label": "Message",
                      "type": "TEXTAREA",
                      "required": true,
                      "supportsExpression": true
                    }
                  ]
                }
                """;
    }

    // =========================================================
    // USER INPUT
    // =========================================================

    /**
     * Returns user input configuration schema.
     *
     * @return JSON schema
     */
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
                      "required": true,
                      "supportsExpression": true
                    }
                  ]
                }
                """;
    }

    // =========================================================
    // AI RESPONSE
    // =========================================================

    /**
     * Returns AI response configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // CONDITION
    // =========================================================

    /**
     * Returns condition configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // API
    // =========================================================

    /**
     * Returns API configuration schema.
     *
     * @return JSON schema
     */
    /**
     * Returns API configuration schema.
     *
     * @return JSON schema
     */
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
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "body",
                  "label": "Request Body",
                  "type": "JSON",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "responseVariable",
                  "label": "Response Variable",
                  "type": "TEXT",
                  "required": false,
                  "supportsExpression": false
                }
              ]
            }
            """;
    }

    // =========================================================
    // WEBHOOK
    // =========================================================

    /**
     * Returns webhook configuration schema.
     *
     * @return JSON schema
     */
    private String webhookSchema() {

        return """
                {
                  "fields": [
                    {
                      "name": "url",
                      "label": "Webhook URL",
                      "type": "TEXT",
                      "required": true,
                      "supportsExpression": true
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

    // =========================================================
    // FUNCTION
    // =========================================================

    /**
     * Returns function configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // KNOWLEDGE BASE
    // =========================================================

    /**
     * Returns knowledge-base configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // RAG
    // =========================================================

    /**
     * Returns RAG configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // SET VARIABLE
    // =========================================================

    /**
     * Returns set-variable configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // TRANSFER
    // =========================================================

    /**
     * Returns transfer configuration schema.
     *
     * @return JSON schema
     */
    /**
     * Returns transfer configuration schema.
     *
     * @return JSON schema
     */
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
                },
                {
                  "name": "message",
                  "label": "Transfer Message",
                  "type": "TEXTAREA",
                  "required": false,
                  "supportsExpression": true
                }
              ]
            }
            """;
    }

    // =========================================================
    // WAIT
    // =========================================================

    /**
     * Returns wait configuration schema.
     *
     * @return JSON schema
     */
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

    // =========================================================
    // STT
    // =========================================================

    /**
     * Returns Speech-to-Text configuration schema.
     *
     * <p>
     * Audio itself is runtime data supplied by the active call
     * session. Therefore the visual builder should configure
     * transcription behavior rather than audio input.
     *
     * @return JSON schema
     */
    /**
     * Returns Speech-to-Text configuration schema.
     *
     * @return JSON schema
     */
    private String sttSchema() {

        return """
            {
              "fields": [
                {
                  "name": "language",
                  "label": "Language",
                  "type": "TEXT",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "finalTranscript",
                  "label": "Final Transcript",
                  "type": "BOOLEAN",
                  "required": false
                }
              ]
            }
            """;
    }

    // =========================================================
    // LLM
    // =========================================================

    /**
     * Returns LLM configuration schema.
     *
     * <p>
     * The current LLM Flow handler obtains the conversation
     * messages from the Flow execution context and delegates
     * generation to LlmRuntimeService.
     *
     * @return JSON schema
     */
    /**
     * Returns LLM configuration schema.
     *
     * @return JSON schema
     */
    /**
     * Returns LLM configuration schema.
     *
     * @return JSON schema
     */
    private String llmSchema() {

        return """
            {
              "fields": [
                {
                  "name": "prompt",
                  "label": "Initial Prompt",
                  "type": "TEXTAREA",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "language",
                  "label": "Language",
                  "type": "TEXT",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "finalResponse",
                  "label": "Final Response",
                  "type": "BOOLEAN",
                  "required": false
                }
              ]
            }
            """;
    }

    // =========================================================
    // TTS
    // =========================================================

    /**
     * Returns Text-to-Speech configuration schema.
     *
     * <p>
     * The text itself is runtime data and normally comes from
     * the previous LLM node. The visual builder therefore
     * configures speech synthesis behavior.
     *
     * @return JSON schema
     */
    /**
     * Returns Text-to-Speech configuration schema.
     *
     * @return JSON schema
     */
    private String ttsSchema() {

        return """
            {
              "fields": [
                {
                  "name": "text",
                  "label": "Text",
                  "type": "TEXTAREA",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "language",
                  "label": "Language",
                  "type": "TEXT",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "speaker",
                  "label": "Speaker",
                  "type": "TEXT",
                  "required": false,
                  "supportsExpression": true
                },
                {
                  "name": "pace",
                  "label": "Speech Pace",
                  "type": "NUMBER",
                  "required": false
                },
                {
                  "name": "speechSampleRate",
                  "label": "Speech Sample Rate",
                  "type": "NUMBER",
                  "required": false
                },
                {
                  "name": "finalResponse",
                  "label": "Final Response",
                  "type": "BOOLEAN",
                  "required": false
                }
              ]
            }
            """;
    }
}
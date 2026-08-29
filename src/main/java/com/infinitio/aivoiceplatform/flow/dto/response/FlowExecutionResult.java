package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Result returned after Flow execution or continuation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowExecutionResult {

    private String executionPublicId;

    private FlowExecutionStatus status;

    private String currentNodeKey;

    private String currentNodeType;

    private String outputText;

    private String action;

    private boolean waitingForInput;

    private boolean waitingForAi;

    private boolean waitingForApi;

    private boolean waitingForTimer;

    private boolean transferred;

    private boolean completed;

    private Map<String, Object> context;
}
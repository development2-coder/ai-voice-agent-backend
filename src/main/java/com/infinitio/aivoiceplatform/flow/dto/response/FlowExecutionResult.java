package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import lombok.*;

import java.util.Map;

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

    private boolean transferred;

    private boolean completed;

    private Map<String, Object> context;
}
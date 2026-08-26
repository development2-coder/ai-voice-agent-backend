package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowNodeExecutionResult {

    private FlowExecutionStatus status;

    private String action;

    private String outputText;

    private boolean waiting;

    private boolean completed;

    private boolean transferred;

    private Map<String, Object> context;
}
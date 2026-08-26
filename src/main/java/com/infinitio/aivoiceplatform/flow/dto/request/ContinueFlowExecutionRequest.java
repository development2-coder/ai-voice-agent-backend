package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContinueFlowExecutionRequest {

    @NotBlank
    private String executionPublicId;

    private String userInput;

    private Map<String, Object> context;
}
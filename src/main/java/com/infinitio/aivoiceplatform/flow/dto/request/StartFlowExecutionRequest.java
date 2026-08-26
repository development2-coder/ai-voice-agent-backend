package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartFlowExecutionRequest {

    @NotBlank
    private String flowPublicId;

    private String callPublicId;

    private String conversationPublicId;

    private Map<String, Object> context;
}
package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFlowEdgeRequest {

    @NotBlank
    private String flowPublicId;

    @NotBlank
    private String sourceNodeKey;

    @NotBlank
    private String targetNodeKey;

    private String conditionExpression;

    private Integer priority;
}
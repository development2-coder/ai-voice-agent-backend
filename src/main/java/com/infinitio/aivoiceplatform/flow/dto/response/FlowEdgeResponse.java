package com.infinitio.aivoiceplatform.flow.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowEdgeResponse {

    private String publicId;

    private String sourceNodeKey;

    private String targetNodeKey;

    private String conditionExpression;

    private Integer priority;
}
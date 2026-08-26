package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowNodeResponse {

    private String publicId;

    private String nodeKey;

    private String name;

    private FlowNodeType nodeType;

    private String configuration;

    private Double positionX;

    private Double positionY;
}
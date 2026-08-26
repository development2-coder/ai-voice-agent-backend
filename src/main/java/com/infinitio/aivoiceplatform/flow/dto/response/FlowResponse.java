package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowResponse {

    private String publicId;

    private String agentPublicId;

    private String name;

    private String description;

    private FlowType flowType;

    private FlowStatus status;

    private Integer version;

    private Boolean active;
}
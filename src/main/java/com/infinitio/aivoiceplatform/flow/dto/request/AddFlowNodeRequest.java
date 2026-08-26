package com.infinitio.aivoiceplatform.flow.dto.request;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFlowNodeRequest {

    @NotBlank
    private String flowPublicId;

    @NotBlank
    @Size(max = 100)
    private String nodeKey;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private FlowNodeType nodeType;

    private String configuration;

    private Double positionX;

    private Double positionY;
}
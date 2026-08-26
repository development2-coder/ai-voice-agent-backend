package com.infinitio.aivoiceplatform.flow.dto.request;

import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFlowRequest {

    @NotBlank
    private String publicId;

    @NotBlank
    private String agentPublicId;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private FlowType flowType;
}
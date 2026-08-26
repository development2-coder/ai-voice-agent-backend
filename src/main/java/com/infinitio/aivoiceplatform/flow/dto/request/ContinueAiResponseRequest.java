package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ContinueAiResponseRequest {

    @NotBlank
    private String executionPublicId;

    private String response;

    private Map<String, Object> context;
}
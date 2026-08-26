package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ContinueApiResponseRequest {

    @NotBlank
    private String executionPublicId;

    private Object response;

    private Map<String, Object> context;
}
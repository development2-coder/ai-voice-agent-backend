package com.infinitio.aivoiceplatform.flow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO representing a supported flow type.
 *
 * @author Infinitio Digital
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowTypeResponse {

    private String code;

    private String displayName;

    private String description;
}
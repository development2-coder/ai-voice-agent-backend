package com.infinitio.aivoiceplatform.callsession.dto.request;

import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request used to update general call session information.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCallSessionRequestDto {

    @Positive
    private Integer agentVersion;

    private String language;

    /**
     * Public identifier of the active flow execution.
     */
    private String flowExecutionPublicId;
}
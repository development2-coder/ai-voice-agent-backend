package com.infinitio.aivoiceplatform.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used to process a DTMF input.
 *
 * <p>
 * The received DTMF value is passed into the active
 * flow context so that flow conditions can determine
 * the next node.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDtmfRequestDto {

    /**
     * Unique public identifier of the call.
     */
    @NotBlank
    private String callId;

    /**
     * DTMF digit received from the caller.
     */
    @NotBlank
    private String digit;
}
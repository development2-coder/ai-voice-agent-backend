package com.infinitio.aivoiceplatform.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used when caller speech interrupts
 * assistant audio playback.
 *
 * <p>
 * The Voice Gateway is responsible for stopping the
 * active audio playback. The orchestrator then updates
 * the conversation runtime state.
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
public class BargeInRequestDto {

    /**
     * Unique public identifier of the call.
     */
    @NotBlank
    private String callId;
}
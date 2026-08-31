package com.infinitio.aivoiceplatform.voicegateway.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO representing a DTMF event received by the
 * Voice Gateway.
 *
 * <p>
 * The Voice Gateway receives DTMF input from the telephony
 * provider and forwards it to the Conversation Orchestrator,
 * which passes it to the tenant-configured Flow.
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
public class VoiceGatewayDtmfRequestDto {

    /**
     * Internal call identifier.
     */
    @NotBlank
    private String callId;

    /**
     * Provider call identifier.
     */
    private String providerCallId;

    /**
     * Provider stream identifier.
     */
    private String streamId;

    /**
     * Sequence number of the provider event.
     */
    private Long sequenceNumber;

    /**
     * DTMF digit received from the caller.
     */
    @NotBlank
    private String digit;

    /**
     * Timestamp supplied by the provider.
     */
    private Long timestamp;
}
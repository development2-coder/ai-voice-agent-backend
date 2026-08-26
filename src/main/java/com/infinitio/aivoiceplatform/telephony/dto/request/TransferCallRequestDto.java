package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for transferring an active call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferCallRequestDto {

    /**
     * Provider-specific call identifier.
     */
    @NotBlank
    private String providerCallId;

    /**
     * Destination number for the transfer.
     */
    @NotBlank
    private String destination;
}
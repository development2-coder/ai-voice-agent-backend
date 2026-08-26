package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for placing an outbound call through a telephony provider.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOutboundCallRequestDto {

    /**
     * Platform Call public identifier.
     *
     * <p>
     * This identifies the master Call record that must be
     * updated with the provider call information.
     * </p>
     */
    @NotBlank
    private String callPublicId;

    /**
     * Number from which the call is placed.
     */
    @NotBlank
    private String fromNumber;

    /**
     * Destination number.
     */
    @NotBlank
    private String toNumber;

    /**
     * Callback URL used by the provider for call events.
     */
    private String callbackUrl;
}
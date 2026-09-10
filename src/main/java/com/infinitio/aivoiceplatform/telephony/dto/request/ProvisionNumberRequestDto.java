package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for provisioning a telephone number through
 * a telephony provider.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionNumberRequestDto {

    /**
     * Exotel phone number that should be provisioned.
     */
    @NotBlank
    private String phoneNumber;

    /**
     * Optional voice URL to associate with the provisioned number.
     */
    private String voiceUrl;

    /**
     * Optional SMS URL to associate with the provisioned number.
     */
    private String smsUrl;

    /**
     * Optional friendly name for the provisioned number.
     */
    private String friendlyName;
}
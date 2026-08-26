package com.infinitio.aivoiceplatform.telephony.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for provisioning a telephone number.
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
     * Region in which the number should be provisioned.
     */
    @NotBlank
    private String region;

    /**
     * Number type such as local or toll-free.
     */
    @NotBlank
    private String type;
}
package com.infinitio.aivoiceplatform.phonenumber.dto.response;

import lombok.*;

/**
 * Phone Number Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberResponse {

    private String publicId;

    private String agentPublicId;

    private String phoneNumber;

    private String displayName;

    private String provider;

    private String providerNumberId;

    private String countryCode;

    private String country;

    private String direction;

    private String description;

    private Integer isActive;
}
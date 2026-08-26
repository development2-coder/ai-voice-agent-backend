package com.infinitio.aivoiceplatform.phonenumber.dto.request;

import com.infinitio.aivoiceplatform.phonenumber.constant.PhoneNumberConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create Phone Number Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhoneNumberRequest {

    @NotBlank(message = "Agent is required.")
    private String agentPublicId;

    @NotBlank(message = "Phone number is required.")
    @Size(max = PhoneNumberConstants.PHONE_NUMBER_MAX_LENGTH)
    private String phoneNumber;

    @Size(max = PhoneNumberConstants.DISPLAY_NAME_MAX_LENGTH)
    private String displayName;

    @NotBlank(message = "Provider is required.")
    @Size(max = PhoneNumberConstants.PROVIDER_MAX_LENGTH)
    private String provider;

    @Size(max = PhoneNumberConstants.PROVIDER_NUMBER_ID_MAX_LENGTH)
    private String providerNumberId;

    @Size(max = PhoneNumberConstants.COUNTRY_CODE_MAX_LENGTH)
    private String countryCode;

    @Size(max = PhoneNumberConstants.COUNTRY_MAX_LENGTH)
    private String country;

    @Size(max = PhoneNumberConstants.DIRECTION_MAX_LENGTH)
    private String direction;

    @Size(max = PhoneNumberConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}
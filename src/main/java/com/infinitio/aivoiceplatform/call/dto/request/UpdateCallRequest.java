package com.infinitio.aivoiceplatform.call.dto.request;

import com.infinitio.aivoiceplatform.call.constant.CallConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Update Call Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCallRequest {

    @NotBlank(message = "Public Id is required.")
    private String publicId;

    @NotBlank(message = "Campaign contact is required.")
    private String campaignContactPublicId;

    @NotBlank(message = "Provider is required.")
    @Size(max = CallConstants.PROVIDER_MAX_LENGTH)
    private String provider;

    @Size(max = CallConstants.PROVIDER_CALL_ID_MAX_LENGTH)
    private String providerCallId;

    @NotBlank(message = "From number is required.")
    @Size(max = CallConstants.FROM_NUMBER_MAX_LENGTH)
    private String fromNumber;

    @NotBlank(message = "To number is required.")
    @Size(max = CallConstants.TO_NUMBER_MAX_LENGTH)
    private String toNumber;

    @NotBlank(message = "Direction is required.")
    @Size(max = CallConstants.DIRECTION_MAX_LENGTH)
    private String direction;

    @Size(max = CallConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
}
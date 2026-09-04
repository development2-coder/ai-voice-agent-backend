package com.infinitio.aivoiceplatform.telephony.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response returned to Exotel for application-controlled
 * call transfer routing.
 *
 * <p>
 * The application provides the destination number that
 * Exotel should use in the subsequent Connect applet.
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
public class ExotelTransferResponseDto {

    /**
     * Destination configuration returned to Exotel.
     */
    private Destination destination;

    /**
     * Destination numbers configuration.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Destination {

        /**
         * Numbers to which the call should be connected.
         */
        private List<String> numbers;
    }
}
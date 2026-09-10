package com.infinitio.aivoiceplatform.telephony.controller;

import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

/**
 * Receives telephony provider webhook events.
 *
 * <p>
 * The Exotel provider can send webhook parameters using either
 * application/x-www-form-urlencoded or multipart/form-data.
 * Both formats are accepted here and converted into the common
 * provider payload format expected by the telephony provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/telephony/webhooks")
@RequiredArgsConstructor
public class TelephonyWebhookController {

    private final TelephonyService telephonyService;

    /**
     * Receives a provider call event.
     *
     * @param providerCode provider code
     * @param formParameters provider webhook parameters
     * @return normalized call event
     */
    @PostMapping(
            value = "/{provider}/call-events",
            consumes = {
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE
            }
    )
    public ResponseEntity<NormalizedCallEventDto>
    receiveCallEvent(
            @PathVariable("provider")
            String providerCode,

            @RequestParam
            MultiValueMap<String, String>
                    formParameters) {

        String payload =
                org.springframework.web.util.UriComponentsBuilder
                        .newInstance()
                        .queryParams(formParameters)
                        .build()
                        .encode()
                        .getQuery();

        NormalizedCallEventDto event =
                telephonyService.processInboundCall(
                        providerCode,
                        payload
                );

        return ResponseEntity.ok(
                event
        );
    }
}
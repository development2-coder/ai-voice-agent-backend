package com.infinitio.aivoiceplatform.telephony.controller;

import com.infinitio.aivoiceplatform.telephony.dto.response.NormalizedCallEventDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives telephony provider webhook events.
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
     * @param payload provider webhook payload
     * @return normalized call event
     */
    @PostMapping("/{provider}/call-events")
    public ResponseEntity<NormalizedCallEventDto>
    receiveCallEvent(
            @PathVariable("provider")
            String providerCode,

            @RequestBody
            String payload) {

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
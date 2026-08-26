package com.infinitio.aivoiceplatform.telephony.controller;

import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.ProvisionNumberRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NumberResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provides REST APIs for provider-independent telephony operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final TelephonyService telephonyService;

    /**
     * Places an outbound call.
     *
     * @param providerCode provider code
     * @param request outbound call request
     * @return provider call response
     */
    @PostMapping("/{provider}/calls")
    public ResponseEntity<ProviderCallResponseDto>
    placeOutboundCall(
            @PathVariable("provider")
            String providerCode,

            @Valid
            @RequestBody
            PlaceOutboundCallRequestDto request) {

        return ResponseEntity.ok(
                telephonyService.placeOutboundCall(
                        providerCode,
                        request
                )
        );
    }

    /**
     * Provisions a telephone number.
     *
     * @param providerCode provider code
     * @param request number provisioning request
     * @return number response
     */
    @PostMapping("/{provider}/numbers")
    public ResponseEntity<NumberResponseDto>
    provisionNumber(
            @PathVariable("provider")
            String providerCode,

            @Valid
            @RequestBody
            ProvisionNumberRequestDto request) {

        return ResponseEntity.ok(
                telephonyService.provisionNumber(
                        providerCode,
                        request
                )
        );
    }

    /**
     * Transfers an active call.
     *
     * @param providerCode provider code
     * @param request transfer request
     * @return empty response
     */
    @PostMapping("/{provider}/calls/transfer")
    public ResponseEntity<Void> transferCall(
            @PathVariable("provider")
            String providerCode,

            @Valid
            @RequestBody
            TransferCallRequestDto request) {

        telephonyService.transferCall(
                providerCode,
                request
        );

        return ResponseEntity.ok()
                .build();
    }

    /**
     * Hangs up an active call.
     *
     * @param providerCode provider code
     * @param request hangup request
     * @return empty response
     */
    @PostMapping("/{provider}/calls/hangup")
    public ResponseEntity<Void> hangupCall(
            @PathVariable("provider")
            String providerCode,

            @Valid
            @RequestBody
            HangupCallRequestDto request) {

        telephonyService.hangupCall(
                providerCode,
                request
        );

        return ResponseEntity.ok()
                .build();
    }
}
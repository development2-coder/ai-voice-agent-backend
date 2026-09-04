package com.infinitio.aivoiceplatform.telephony.controller;

import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.ProvisionNumberRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.NumberResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.ProviderCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.AgentOutboundCallService;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;
import com.infinitio.aivoiceplatform.telephony.dto.request.PlaceAgentOutboundCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.dto.response.AgentOutboundCallResponseDto;
import com.infinitio.aivoiceplatform.telephony.dto.request.NumberSearchRequestDto;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provides REST APIs for provider-independent telephony operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final TelephonyService telephonyService;

    private final AgentOutboundCallService
            agentOutboundCallService;

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

    /**
     * Places a direct outbound call using an Agent Flow.
     *
     * <p>
     * The telephony provider is resolved from the phone number
     * assigned to the Agent. The controller therefore remains
     * independent of any specific CPaaS provider.
     * </p>
     *
     * @param request Agent outbound call request
     * @return outbound call response
     */
    @PostMapping("/agent-calls")
    public ResponseEntity<AgentOutboundCallResponseDto>
    placeAgentOutboundCall(
            @Valid
            @RequestBody
            PlaceAgentOutboundCallRequestDto request) {

        log.info(
                "REST Request : Place Agent Outbound Call. "
                        + "flowPublicId={}, phoneNumberPublicId={}, "
                        + "toNumber={}",
                request.getFlowPublicId(),
                request.getPhoneNumberPublicId(),
                request.getToNumber()
        );

        AgentOutboundCallResponseDto response =
                agentOutboundCallService
                        .placeAgentOutboundCall(
                                request
                        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves phone numbers already owned by the provider.
     *
     * @param providerCode provider code
     * @return owned provider numbers
     */
    @GetMapping("/{provider}/numbers")
    public ResponseEntity<List<NumberResponseDto>>
    getOwnedNumbers(
            @PathVariable("provider")
            String providerCode) {

        log.info(
                "REST Request : Get Provider Owned Numbers. provider={}",
                providerCode
        );

        return ResponseEntity.ok(
                telephonyService.getOwnedNumbers(
                        providerCode
                )
        );
    }

    /**
     * Retrieves phone numbers available for provisioning.
     *
     * @param providerCode provider code
     * @param request number search criteria
     * @return available provider numbers
     */
    @PostMapping("/{provider}/numbers/available")
    public ResponseEntity<List<NumberResponseDto>>
    getAvailableNumbers(
            @PathVariable("provider")
            String providerCode,

            @Valid
            @RequestBody
            NumberSearchRequestDto request) {

        log.info(
                "REST Request : Get Available Provider Numbers. provider={}",
                providerCode
        );

        return ResponseEntity.ok(
                telephonyService.getAvailableNumbers(
                        providerCode,
                        request
                )
        );
    }
}
package com.infinitio.aivoiceplatform.telephony.controller;

import com.infinitio.aivoiceplatform.telephony.dto.response.ExotelTransferResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.ExotelTransferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides application-side transfer routing APIs for Exotel.
 *
 * <p>
 * Exotel uses this endpoint during the transfer handoff to
 * retrieve the destination selected by the application Flow.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/telephony/exotel")
@RequiredArgsConstructor
public class ExotelTransferController {

    private final ExotelTransferService exotelTransferService;

    /**
     * Retrieves the destination configured for an active
     * Exotel transfer.
     *
     * @param callSid Exotel CallSid
     * @return transfer destination
     */
    @GetMapping("/transfer")
    public ResponseEntity<ExotelTransferResponseDto>
    getTransferDestination(
            @RequestParam("CallSid")
            String callSid) {

        log.info(
                "Exotel transfer destination requested. "
                        + "callSid={}",
                callSid
        );

        ExotelTransferResponseDto response =
                exotelTransferService
                        .getTransferDestination(callSid);

        return ResponseEntity.ok(response);
    }
}
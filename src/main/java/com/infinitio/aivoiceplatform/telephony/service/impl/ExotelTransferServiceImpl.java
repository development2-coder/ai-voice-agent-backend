package com.infinitio.aivoiceplatform.telephony.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.telephony.dto.response.ExotelTransferResponseDto;
import com.infinitio.aivoiceplatform.telephony.service.ExotelTransferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Default implementation of application-side Exotel
 * transfer routing.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExotelTransferServiceImpl
        implements ExotelTransferService {

    private final CallRepository callRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ExotelTransferResponseDto getTransferDestination(
            String providerCallId) {

        if (providerCallId == null
                || providerCallId.isBlank()) {

            log.warn(
                    "Transfer destination requested without "
                            + "provider call ID."
            );

            throw new IllegalArgumentException(
                    "Provider call ID is required."
            );
        }

        log.info(
                "Resolving transfer destination. "
                        + "providerCallId={}",
                providerCallId
        );

        Call call =
                callRepository
                        .findByProviderCallId(providerCallId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Call not found while resolving "
                                            + "transfer destination. "
                                            + "providerCallId={}",
                                    providerCallId
                            );

                            return new IllegalStateException(
                                    "Call not found for provider call ID."
                            );
                        });

        if (!Boolean.TRUE.equals(
                call.getTransferRequested())) {

            log.warn(
                    "Transfer was not requested for call. "
                            + "providerCallId={}",
                    providerCallId
            );

            throw new IllegalStateException(
                    "Transfer was not requested for this call."
            );
        }

        String destination =
                call.getTransferDestination();

        if (destination == null
                || destination.isBlank()) {

            log.error(
                    "Transfer destination is missing. "
                            + "providerCallId={}",
                    providerCallId
            );

            throw new IllegalStateException(
                    "Transfer destination is not available."
            );
        }

        log.info(
                "Transfer destination resolved successfully. "
                        + "providerCallId={}, destination={}",
                providerCallId,
                destination
        );

        return ExotelTransferResponseDto.builder()
                .destination(
                        ExotelTransferResponseDto.Destination
                                .builder()
                                .numbers(
                                        java.util.List.of(
                                                destination
                                        )
                                )
                                .build()
                )
                .build();
    }
}
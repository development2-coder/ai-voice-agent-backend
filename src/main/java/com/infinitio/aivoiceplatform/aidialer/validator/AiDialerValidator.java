package com.infinitio.aivoiceplatform.aidialer.validator;

import com.infinitio.aivoiceplatform.aidialer.dto.request.CreateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.UpdateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.repository.AiDialerRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiDialerValidator {

    private static final Integer NOT_DELETED = 0;

    private final AiDialerRepository aiDialerRepository;


    // =========================================================
    // VALIDATE CREATE
    // =========================================================

    public void validateForCreate(
            CreateAiDialerRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "AI Dialer request cannot be null."
            );
        }

        if (request.getDialerName() == null
                || request.getDialerName().isBlank()) {

            throw new BadRequestException(
                    "Dialer name is required."
            );
        }

        if (request.getCampaignPublicId() == null
                || request.getCampaignPublicId().isBlank()) {

            throw new BadRequestException(
                    "Campaign public ID is required."
            );
        }

        if (request.getAgentPublicId() == null
                || request.getAgentPublicId().isBlank()) {

            throw new BadRequestException(
                    "Agent public ID is required."
            );
        }

        if (request.getFlowPublicId() == null
                || request.getFlowPublicId().isBlank()) {

            throw new BadRequestException(
                    "Flow public ID is required."
            );

        }

        validateSchedule(
                request.getScheduledStartAt(),
                request.getScheduledEndAt()
        );

        log.info(
                "AI Dialer create validation completed successfully."
        );
    }


    // =========================================================
    // VALIDATE UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateAiDialerRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "AI Dialer update request cannot be null."
            );
        }

        /*
         * Update request does not contain publicId
         * in the current DTO.
         *
         * Therefore entity lookup must be handled
         * by the service using the actual publicId
         * supplied through the service/controller flow.
         */

        validateSchedule(
                request.getScheduledStartAt(),
                request.getScheduledEndAt()
        );

        log.info(
                "AI Dialer update validation completed successfully."
        );
    }


    // =========================================================
    // VALIDATE AND GET
    // =========================================================

    public AiDialer validateAndGet(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "AI Dialer public ID is required."
            );
        }

        return aiDialerRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "AI Dialer not found."
                        )
                );
    }


    // =========================================================
    // VALIDATE SCHEDULE
    // =========================================================

    private void validateSchedule(
            java.time.LocalDateTime scheduledStartAt,
            java.time.LocalDateTime scheduledEndAt) {

        if (scheduledStartAt != null
                && scheduledEndAt != null
                && !scheduledEndAt.isAfter(
                scheduledStartAt
        )) {

            throw new BadRequestException(
                    "Scheduled end time must be after "
                            + "scheduled start time."
            );
        }
    }
}
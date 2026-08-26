package com.infinitio.aivoiceplatform.transcript.mapper;

import org.springframework.stereotype.Component;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.response.TranscriptResponse;
import com.infinitio.aivoiceplatform.transcript.entity.Transcript;

/**
 * Mapper for Transcript entity and DTO objects.
 *
 * <p>
 * This mapper is responsible only for converting between
 * request/response DTOs and Transcript entities.
 * Database lookups are handled by the service layer.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
public class TranscriptMapper {

    /**
     * Converts create request into Transcript entity.
     *
     * <p>
     * The associated Call entity is resolved by the service
     * layer and passed to this mapper.
     * </p>
     *
     * @param request transcript creation request
     * @param call associated call entity
     * @return transcript entity
     */
    public Transcript toEntity(
            CreateTranscriptRequest request,
            Call call) {

        return Transcript.builder()
                .call(call)
                .sequenceNumber(
                        request.getSequenceNumber()
                )
                .speakerType(
                        request.getSpeakerType()
                )
                .text(
                        request.getText()
                )
                .language(
                        request.getLanguage()
                )
                .source(
                        request.getSource()
                )
                .startedAt(
                        request.getStartedAt()
                )
                .endedAt(
                        request.getEndedAt()
                )
                .build();
    }

    /**
     * Updates a Transcript entity from an update request.
     *
     * <p>
     * The associated Call is intentionally not changed.
     * </p>
     *
     * @param request update request
     * @param transcript transcript entity
     */
    public void updateEntity(
            UpdateTranscriptRequest request,
            Transcript transcript) {

        if (request.getSequenceNumber() != null) {

            transcript.setSequenceNumber(
                    request.getSequenceNumber()
            );
        }

        if (request.getSpeakerType() != null) {

            transcript.setSpeakerType(
                    request.getSpeakerType()
            );
        }

        if (request.getText() != null) {

            transcript.setText(
                    request.getText()
            );
        }

        if (request.getLanguage() != null) {

            transcript.setLanguage(
                    request.getLanguage()
            );
        }

        if (request.getSource() != null) {

            transcript.setSource(
                    request.getSource()
            );
        }

        if (request.getStartedAt() != null) {

            transcript.setStartedAt(
                    request.getStartedAt()
            );
        }

        if (request.getEndedAt() != null) {

            transcript.setEndedAt(
                    request.getEndedAt()
            );
        }
    }

    /**
     * Converts Transcript entity into response DTO.
     *
     * @param transcript transcript entity
     * @return transcript response
     */
    public TranscriptResponse toResponse(
            Transcript transcript) {

        return TranscriptResponse.builder()
                .publicId(
                        transcript.getPublicId()
                )
                .callPublicId(
                        transcript.getCall()
                                .getPublicId()
                )
                .sequenceNumber(
                        transcript.getSequenceNumber()
                )
                .speakerType(
                        transcript.getSpeakerType()
                )
                .text(
                        transcript.getText()
                )
                .language(
                        transcript.getLanguage()
                )
                .source(
                        transcript.getSource()
                )
                .startedAt(
                        transcript.getStartedAt()
                )
                .endedAt(
                        transcript.getEndedAt()
                )
                .isActive(
                        transcript.getIsActive()
                )
                .isDeleted(
                        transcript.getIsDeleted()
                )
                .createdAt(
                        transcript.getCreatedAt()
                )
                .updatedAt(
                        transcript.getUpdatedAt()
                )
                .build();
    }
}
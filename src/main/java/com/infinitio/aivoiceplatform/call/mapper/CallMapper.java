package com.infinitio.aivoiceplatform.call.mapper;

import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;
import com.infinitio.aivoiceplatform.call.entity.Call;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CallMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "campaignContact", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "answeredAt", ignore = true)
    @Mapping(target = "endedAt", ignore = true)
    @Mapping(target = "durationSeconds", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "recordingUrl", ignore = true)
    @Mapping(target = "transcriptFilePath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Call toEntity(CreateCallRequest request);

    @Mapping(
            target = "campaignContactPublicId",
            source = "campaignContact.publicId"
    )
    @Mapping(
            target = "durationSeconds",
            expression = "java(resolveDurationSeconds(entity))"
    )
    @Mapping(
            target = "duration",
            expression = "java(formatDuration(entity))"
    )
    CallResponse toResponse(Call entity);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "campaignContact", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "answeredAt", ignore = true)
    @Mapping(target = "endedAt", ignore = true)
    @Mapping(target = "durationSeconds", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "recordingUrl", ignore = true)
    @Mapping(target = "transcriptFilePath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntity(
            UpdateCallRequest request,
            @MappingTarget Call entity
    );

    /**
     * Resolves duration using the stored value first, then timestamps.
     * If the call is still active, calculates elapsed seconds until now.
     */
    default Integer resolveDurationSeconds(Call call) {

        if (call == null) {
            return null;
        }

        if (call.getDurationSeconds() != null) {
            return Math.max(call.getDurationSeconds(), 0);
        }

        java.time.LocalDateTime start =
                call.getAnsweredAt() != null
                        ? call.getAnsweredAt()
                        : call.getStartedAt();

        if (start == null) {
            return null;
        }

        java.time.LocalDateTime end =
                call.getEndedAt() != null
                        ? call.getEndedAt()
                        : java.time.LocalDateTime.now();

        long seconds =
                java.time.Duration.between(start, end).getSeconds();

        return (int) Math.min(
                Math.max(seconds, 0),
                Integer.MAX_VALUE
        );
    }

    /**
     * Formats duration as HH:mm:ss.
     */
    default String formatDuration(Call call) {

        Integer seconds = resolveDurationSeconds(call);

        if (seconds == null) {
            return null;
        }

        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;

        return String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                remainingSeconds
        );
    }
}
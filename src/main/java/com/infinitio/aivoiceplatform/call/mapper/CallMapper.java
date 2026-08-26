package com.infinitio.aivoiceplatform.call.mapper;

import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;
import com.infinitio.aivoiceplatform.call.entity.Call;
import org.mapstruct.*;

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

    // =========================================================
    // CREATE
    // =========================================================

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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Call toEntity(
            CreateCallRequest request
    );


    // =========================================================
    // RESPONSE
    // =========================================================

    @Mapping(
            target = "campaignContactPublicId",
            source = "campaignContact.publicId"
    )
    CallResponse toResponse(
            Call entity
    );


    // =========================================================
    // UPDATE
    // =========================================================

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
}
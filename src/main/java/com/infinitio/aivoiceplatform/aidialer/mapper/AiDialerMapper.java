package com.infinitio.aivoiceplatform.aidialer.mapper;

import com.infinitio.aivoiceplatform.aidialer.dto.request.CreateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.UpdateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerResponse;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for AI Dialer entities and DTOs.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AiDialerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "flow", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "pausedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    AiDialer toEntity(
            CreateAiDialerRequest request
    );

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "flow", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "pausedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(
            UpdateAiDialerRequest request,
            @MappingTarget AiDialer entity
    );

    @Mapping(
            target = "campaignPublicId",
            source = "campaign.publicId"
    )
    @Mapping(
            target = "campaignName",
            source = "campaign.campaignName"
    )
    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    @Mapping(
            target = "agentName",
            source = "agent.agentName"
    )
    @Mapping(
            target = "flowPublicId",
            source = "flow.publicId"
    )
    @Mapping(
            target = "flowName",
            source = "flow.name"
    )
    DialerResponse toResponse(
            AiDialer entity
    );

    @Mapping(
            target = "dialerPublicId",
            source = "dialer.publicId"
    )
    @Mapping(
            target = "campaignContactPublicId",
            source = "campaignContact.publicId"
    )
    DialerCallResponse toCallResponse(
            DialerCall entity
    );
}
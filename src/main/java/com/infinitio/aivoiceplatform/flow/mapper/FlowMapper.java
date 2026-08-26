package com.infinitio.aivoiceplatform.flow.mapper;

import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FlowMapper {

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "agent",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "version",
            ignore = true
    )
    Flow toEntity(
            CreateFlowRequest request
    );


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    @Mapping(
            target = "active",
            expression =
                    "java(mapIntegerToBoolean(entity.getIsActive()))"
    )
    FlowResponse toResponse(
            Flow entity
    );


    // =========================================================
    // UPDATE
    // =========================================================

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "agent",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "version",
            ignore = true
    )
    void updateEntity(
            UpdateFlowRequest request,
            @MappingTarget Flow entity
    );


    default Boolean mapIntegerToBoolean(
            Integer value) {

        if (value == null) {
            return null;
        }

        return value == 1;
    }
}
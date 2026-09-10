package com.infinitio.aivoiceplatform.flow.mapper;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for Flow node entities and DTOs.
 *
 * <p>
 * Handles conversion between Flow node request/response DTOs
 * and the persistent {@link FlowNode} entity.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface FlowNodeMapper {

    /**
     * Converts an Add Flow Node request to a Flow node entity.
     *
     * <p>
     * Position coordinates are explicitly mapped so that the
     * visual Flow Builder coordinates are persisted correctly.
     * </p>
     *
     * @param request node creation request
     * @return Flow node entity
     */
    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "flow",
            ignore = true
    )
    @Mapping(
            target = "positionX",
            source = "positionX"
    )
    @Mapping(
            target = "positionY",
            source = "positionY"
    )
    FlowNode toEntity(
            AddFlowNodeRequest request
    );

    /**
     * Converts a Flow node entity to a response DTO.
     *
     * <p>
     * Position coordinates are explicitly mapped so that the
     * Flow Builder receives the persisted visual coordinates.
     * </p>
     *
     * @param entity Flow node entity
     * @return Flow node response
     */
    @Mapping(
            target = "positionX",
            source = "positionX"
    )
    @Mapping(
            target = "positionY",
            source = "positionY"
    )
    FlowNodeResponse toResponse(
            FlowNode entity
    );

    /**
     * Updates an existing Flow node entity.
     *
     * <p>
     * Null properties from the request are ignored, while
     * supplied position coordinates are explicitly persisted.
     * </p>
     *
     * @param request node update request
     * @param entity existing Flow node entity
     */
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
            target = "flow",
            ignore = true
    )
    @Mapping(
            target = "positionX",
            source = "positionX"
    )
    @Mapping(
            target = "positionY",
            source = "positionY"
    )
    void updateEntity(
            UpdateFlowNodeRequest request,
            @MappingTarget FlowNode entity
    );
}
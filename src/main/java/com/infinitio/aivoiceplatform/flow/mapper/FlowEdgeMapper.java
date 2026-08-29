package com.infinitio.aivoiceplatform.flow.mapper;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps Flow edge request/entity/response objects.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface FlowEdgeMapper {

    /**
     * Converts an add-edge request to an entity.
     *
     * <p>
     * Flow and node relationships are assigned by the service
     * after validation.
     * </p>
     *
     * @param request edge request
     * @return FlowEdge entity
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
            target = "isActive",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "createdBy",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    @Mapping(
            target = "updatedBy",
            ignore = true
    )
    @Mapping(
            target = "isDeleted",
            ignore = true
    )
    @Mapping(
            target = "deletedAt",
            ignore = true
    )
    @Mapping(
            target = "flow",
            ignore = true
    )
    @Mapping(
            target = "sourceNode",
            ignore = true
    )
    @Mapping(
            target = "targetNode",
            ignore = true
    )
    FlowEdge toEntity(
            AddFlowEdgeRequest request
    );

    /**
     * Converts a FlowEdge entity to its API response.
     *
     * @param entity edge entity
     * @return edge response
     */
    @Mapping(
            target = "sourceNodeKey",
            source = "sourceNode.nodeKey"
    )
    @Mapping(
            target = "targetNodeKey",
            source = "targetNode.nodeKey"
    )
    FlowEdgeResponse toResponse(
            FlowEdge entity
    );
}
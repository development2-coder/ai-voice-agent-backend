package com.infinitio.aivoiceplatform.flow.mapper;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FlowEdgeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "flow", ignore = true)
    @Mapping(target = "sourceNode", ignore = true)
    @Mapping(target = "targetNode", ignore = true)
    FlowEdge toEntity(
            AddFlowEdgeRequest request
    );

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
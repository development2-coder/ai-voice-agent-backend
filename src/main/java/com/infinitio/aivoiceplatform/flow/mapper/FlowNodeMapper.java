package com.infinitio.aivoiceplatform.flow.mapper;

import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeResponse;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FlowNodeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "flow", ignore = true)
    FlowNode toEntity(
            AddFlowNodeRequest request
    );

    FlowNodeResponse toResponse(
            FlowNode entity
    );

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "flow", ignore = true)
    void updateEntity(
            UpdateFlowNodeRequest request,
            @MappingTarget FlowNode entity
    );
}
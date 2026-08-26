package com.infinitio.aivoiceplatform.agentconfig.mapper;

import com.infinitio.aivoiceplatform.agentconfig.dto.request.CreateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.UpdateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigResponse;
import com.infinitio.aivoiceplatform.agentconfig.entity.AgentConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Agent Configuration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AgentConfigMapper {


    // =========================================================
    // CREATE
    // =========================================================

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
            target = "isActive",
            ignore = true
    )
    @Mapping(
            target = "isDeleted",
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
            target = "deletedAt",
            ignore = true
    )
    AgentConfig toEntity(
            CreateAgentConfigRequest request
    );


    // =========================================================
    // RESPONSE
    // =========================================================

    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    AgentConfigResponse toResponse(
            AgentConfig entity
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
            target = "isActive",
            ignore = true
    )
    @Mapping(
            target = "isDeleted",
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
            target = "deletedAt",
            ignore = true
    )
    void updateEntity(
            UpdateAgentConfigRequest request,
            @MappingTarget AgentConfig entity
    );
}
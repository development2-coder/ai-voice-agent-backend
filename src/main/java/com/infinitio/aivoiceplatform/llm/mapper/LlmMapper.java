package com.infinitio.aivoiceplatform.llm.mapper;

import com.infinitio.aivoiceplatform.llm.dto.request.CreateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.request.UpdateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.response.LlmResponse;
import com.infinitio.aivoiceplatform.llm.entity.Llm;
import org.mapstruct.*;

/**
 * Mapper for LLM.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LlmMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Llm toEntity(CreateLlmRequest request);

    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    LlmResponse toResponse(Llm entity);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntity(
            UpdateLlmRequest request,
            @MappingTarget Llm entity
    );
}
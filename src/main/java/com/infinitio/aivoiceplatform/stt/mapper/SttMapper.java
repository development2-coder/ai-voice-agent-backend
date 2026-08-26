package com.infinitio.aivoiceplatform.stt.mapper;

import com.infinitio.aivoiceplatform.stt.dto.request.CreateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.request.UpdateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.response.SttResponse;
import com.infinitio.aivoiceplatform.stt.entity.Stt;
import org.mapstruct.*;

/**
 * Mapper for STT.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SttMapper {

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
    Stt toEntity(CreateSttRequest request);

    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    SttResponse toResponse(Stt entity);

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
            UpdateSttRequest request,
            @MappingTarget Stt entity
    );
}
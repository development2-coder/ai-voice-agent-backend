package com.infinitio.aivoiceplatform.voice.mapper;

import com.infinitio.aivoiceplatform.voice.dto.request.CreateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.request.UpdateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.response.VoiceResponse;
import com.infinitio.aivoiceplatform.voice.entity.Voice;
import org.mapstruct.*;

/**
 * Mapper for Voice.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface VoiceMapper {

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
    Voice toEntity(CreateVoiceRequest request);

    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    VoiceResponse toResponse(Voice entity);

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
            UpdateVoiceRequest request,
            @MappingTarget Voice entity
    );
}
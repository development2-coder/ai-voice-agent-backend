package com.infinitio.aivoiceplatform.tts.mapper;

import com.infinitio.aivoiceplatform.tts.dto.request.CreateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.request.UpdateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.response.TtsResponse;
import com.infinitio.aivoiceplatform.tts.entity.Tts;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for TTS.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface TtsMapper {

    /**
     * Converts create request into TTS entity.
     *
     * @param request create TTS request
     * @return TTS entity
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
            target = "agent",
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
    @Mapping(
            target = "isDeleted",
            ignore = true
    )
    @Mapping(
            target = "isActive",
            ignore = true
    )
    Tts toEntity(
            CreateTtsRequest request
    );

    /**
     * Converts TTS entity into response.
     *
     * @param entity TTS entity
     * @return TTS response
     */
    @Mapping(
            target = "agentPublicId",
            source = "agent.publicId"
    )
    TtsResponse toResponse(
            Tts entity
    );

    /**
     * Updates editable TTS fields.
     *
     * @param request update request
     * @param entity TTS entity
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
            target = "agent",
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
    @Mapping(
            target = "isDeleted",
            ignore = true
    )
    @Mapping(
            target = "isActive",
            ignore = true
    )
    void updateEntity(
            UpdateTtsRequest request,
            @MappingTarget Tts entity
    );
}
package com.infinitio.aivoiceplatform.campaigncontact.mapper;

import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.UpdateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactResponse;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import org.mapstruct.*;

/**
 * Mapper for Campaign Contact.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface CampaignContactMapper {

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "publicId",
            ignore = true
    )
    @Mapping(
            target = "campaign",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "attemptCount",
            ignore = true
    )
    @Mapping(
            target = "lastAttemptAt",
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
    CampaignContact toEntity(
            CreateCampaignContactRequest request
    );

    @Mapping(
            target = "campaignPublicId",
            source = "campaign.publicId"
    )
    CampaignContactResponse toResponse(
            CampaignContact entity
    );

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
            target = "campaign",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "attemptCount",
            ignore = true
    )
    @Mapping(
            target = "lastAttemptAt",
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
            UpdateCampaignContactRequest request,
            @MappingTarget CampaignContact entity
    );
}
package com.infinitio.aivoiceplatform.organization.organizationbranding.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.organization.organizationbranding.constant.OrganizationBrandingMessages;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.CreateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.UpdateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.entity.OrganizationBranding;
import com.infinitio.aivoiceplatform.organization.organizationbranding.repository.OrganizationBrandingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationBrandingValidator {

    private final OrganizationBrandingRepository organizationBrandingRepository;

    /**
     * Validate Create Request.
     */
    public void validateForCreate(CreateOrganizationBrandingRequest request) {

        log.info("Validating Create Organization Branding Request.");

        if (organizationBrandingRepository.existsByOrganization_PublicId(
                request.getOrganizationPublicId())) {

            throw new BadRequestException(
                    OrganizationBrandingMessages.ORGANIZATION_ALREADY_HAS_BRANDING);
        }

    }

    /**
     * Validate Update Request.
     */
    public void validateForUpdate(UpdateOrganizationBrandingRequest request) {

        log.info("Validating Update Organization Branding Request.");

        validateAndGet(request.getPublicId());

    }

    /**
     * Validate Branding Exists.
     */
    public OrganizationBranding validateAndGet(String publicId) {

        return organizationBrandingRepository.findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                OrganizationBrandingMessages.BRANDING_NOT_FOUND));

    }

}
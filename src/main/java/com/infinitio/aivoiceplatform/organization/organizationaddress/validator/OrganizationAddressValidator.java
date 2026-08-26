package com.infinitio.aivoiceplatform.organization.organizationaddress.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.organization.organizationaddress.constant.OrganizationAddressMessages;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.CreateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.UpdateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.entity.OrganizationAddress;
import com.infinitio.aivoiceplatform.organization.organizationaddress.repository.OrganizationAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationAddressValidator {

    private final OrganizationAddressRepository repository;

    public void validateForCreate(CreateOrganizationAddressRequest request) {

        log.info("Validating Create Organization Address.");

        if (repository.existsByOrganization_PublicId(
                request.getOrganizationPublicId())) {

            throw new BadRequestException(
                    OrganizationAddressMessages.ORGANIZATION_ALREADY_HAS_ADDRESS);
        }

    }

    public void validateForUpdate(UpdateOrganizationAddressRequest request) {

        log.info("Validating Update Organization Address.");

        validateAndGet(request.getPublicId());

    }

    public OrganizationAddress validateAndGet(String publicId) {

        return repository.findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                OrganizationAddressMessages.ADDRESS_NOT_FOUND));

    }

}
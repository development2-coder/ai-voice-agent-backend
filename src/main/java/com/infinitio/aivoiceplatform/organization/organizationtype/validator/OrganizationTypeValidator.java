package com.infinitio.aivoiceplatform.organization.organizationtype.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.organization.organizationtype.constant.OrganizationTypeConstants;
import com.infinitio.aivoiceplatform.organization.organizationtype.constant.OrganizationTypeMessages;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.CreateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.UpdateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.entity.OrganizationType;
import com.infinitio.aivoiceplatform.organization.organizationtype.repository.OrganizationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Organization Type Validator.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class OrganizationTypeValidator {

    private final OrganizationTypeRepository repository;


    // =========================================================
    // CREATE VALIDATION
    // =========================================================

    public void validateForCreate(
            CreateOrganizationTypeRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Organization type request cannot be null."
            );
        }

        String code =
                normalizeCode(
                        request.getOrganizationTypeCode()
                );

        String name =
                normalizeName(
                        request.getOrganizationTypeName()
                );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        if (repository
                .existsByOrganizationTypeCodeAndIsDeleted(
                        code,
                        OrganizationTypeConstants.NOT_DELETED
                )) {

            throw new ConflictException(
                    OrganizationTypeMessages.CODE_ALREADY_EXISTS
            );
        }

        if (repository
                .existsByOrganizationTypeNameAndIsDeleted(
                        name,
                        OrganizationTypeConstants.NOT_DELETED
                )) {

            throw new ConflictException(
                    OrganizationTypeMessages.NAME_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE VALIDATION
    // =========================================================

    public void validateForUpdate(
            UpdateOrganizationTypeRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Organization type update request cannot be null."
            );
        }

        if (isBlank(request.getPublicId())) {

            throw new BadRequestException(
                    OrganizationTypeMessages.INVALID_PUBLIC_ID
            );
        }

        String publicId =
                request.getPublicId().trim();

        String code =
                normalizeCode(
                        request.getOrganizationTypeCode()
                );

        String name =
                normalizeName(
                        request.getOrganizationTypeName()
                );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        validateAndGet(publicId);

        if (repository
                .existsByOrganizationTypeCodeAndIsDeletedAndPublicIdNot(
                        code,
                        OrganizationTypeConstants.NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    OrganizationTypeMessages.CODE_ALREADY_EXISTS
            );
        }

        if (repository
                .existsByOrganizationTypeNameAndIsDeletedAndPublicIdNot(
                        name,
                        OrganizationTypeConstants.NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    OrganizationTypeMessages.NAME_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // FIND
    // =========================================================

    public OrganizationType validateAndGet(
            String publicId) {

        if (isBlank(publicId)) {

            throw new BadRequestException(
                    OrganizationTypeMessages.INVALID_PUBLIC_ID
            );
        }

        return repository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        OrganizationTypeConstants.NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                OrganizationTypeMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // DISPLAY ORDER
    // =========================================================

    private void validateDisplayOrder(
            Integer displayOrder) {

        if (displayOrder == null
                || displayOrder
                < OrganizationTypeConstants.MIN_DISPLAY_ORDER) {

            throw new BadRequestException(
                    OrganizationTypeMessages.INVALID_DISPLAY_ORDER
            );
        }
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalizeCode(
            String code) {

        if (isBlank(code)) {

            throw new BadRequestException(
                    "Organization type code is required."
            );
        }

        return code.trim().toUpperCase();
    }


    private String normalizeName(
            String name) {

        if (isBlank(name)) {

            throw new BadRequestException(
                    "Organization type name is required."
            );
        }

        return name.trim();
    }


    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
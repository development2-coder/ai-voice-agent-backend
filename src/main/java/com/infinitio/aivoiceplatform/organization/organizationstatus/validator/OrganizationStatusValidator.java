package com.infinitio.aivoiceplatform.organization.organizationstatus.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.organization.organizationstatus.constant.OrganizationStatusConstants;
import com.infinitio.aivoiceplatform.organization.organizationstatus.constant.OrganizationStatusMessages;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.CreateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.UpdateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.entity.OrganizationStatus;
import com.infinitio.aivoiceplatform.organization.organizationstatus.repository.OrganizationStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Organization Status Validator.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class OrganizationStatusValidator {

    private final OrganizationStatusRepository repository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateOrganizationStatusRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Organization status request cannot be null."
            );
        }

        String code =
                normalizeCode(
                        request.getOrganizationStatusCode()
                );

        String name =
                normalizeName(
                        request.getOrganizationStatusName()
                );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        if (repository
                .existsByOrganizationStatusCodeAndIsDeleted(
                        code,
                        OrganizationStatusConstants.NOT_DELETED
                )) {

            throw new ConflictException(
                    OrganizationStatusMessages.CODE_ALREADY_EXISTS
            );
        }

        if (repository
                .existsByOrganizationStatusNameAndIsDeleted(
                        name,
                        OrganizationStatusConstants.NOT_DELETED
                )) {

            throw new ConflictException(
                    OrganizationStatusMessages.NAME_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateOrganizationStatusRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Organization status update request cannot be null."
            );
        }

        if (isBlank(request.getPublicId())) {
            throw new BadRequestException(
                    OrganizationStatusMessages.INVALID_PUBLIC_ID
            );
        }

        String publicId =
                request.getPublicId().trim();

        String code =
                normalizeCode(
                        request.getOrganizationStatusCode()
                );

        String name =
                normalizeName(
                        request.getOrganizationStatusName()
                );

        validateDisplayOrder(
                request.getDisplayOrder()
        );

        validateAndGet(publicId);

        if (repository
                .existsByOrganizationStatusCodeAndIsDeletedAndPublicIdNot(
                        code,
                        OrganizationStatusConstants.NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    OrganizationStatusMessages.CODE_ALREADY_EXISTS
            );
        }

        if (repository
                .existsByOrganizationStatusNameAndIsDeletedAndPublicIdNot(
                        name,
                        OrganizationStatusConstants.NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    OrganizationStatusMessages.NAME_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // FIND
    // =========================================================

    public OrganizationStatus validateAndGet(
            String publicId) {

        if (isBlank(publicId)) {
            throw new BadRequestException(
                    OrganizationStatusMessages.INVALID_PUBLIC_ID
            );
        }

        return repository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        OrganizationStatusConstants.NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                OrganizationStatusMessages.NOT_FOUND
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
                < OrganizationStatusConstants.MIN_DISPLAY_ORDER) {

            throw new BadRequestException(
                    OrganizationStatusMessages.INVALID_DISPLAY_ORDER
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
                    "Organization status code is required."
            );
        }

        return code.trim().toUpperCase();
    }


    private String normalizeName(
            String name) {

        if (isBlank(name)) {
            throw new BadRequestException(
                    "Organization status name is required."
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
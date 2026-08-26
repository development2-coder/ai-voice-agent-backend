package com.infinitio.aivoiceplatform.organization.organization.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.organization.organization.constant.OrganizationConstants;
import com.infinitio.aivoiceplatform.organization.organization.constant.OrganizationMessages;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.CreateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.UpdateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.entity.Organization;
import com.infinitio.aivoiceplatform.organization.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Organization Validator.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class OrganizationValidator {

    private final OrganizationRepository organizationRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateOrganizationRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Organization request cannot be null."
            );
        }

        if (isBlank(request.getOrganizationCode())) {
            throw new BadRequestException(
                    "Organization code is required."
            );
        }

        if (isBlank(request.getOrganizationName())) {
            throw new BadRequestException(
                    "Organization name is required."
            );
        }

        if (isBlank(request.getOrganizationTypePublicId())) {
            throw new BadRequestException(
                    OrganizationMessages.TYPE_REQUIRED
            );
        }

        if (isBlank(request.getOrganizationStatusPublicId())) {
            throw new BadRequestException(
                    OrganizationMessages.STATUS_REQUIRED
            );
        }

        String code =
                normalizeCode(
                        request.getOrganizationCode()
                );

        if (organizationRepository
                .existsByOrganizationCodeAndIsDeleted(
                        code,
                        OrganizationConstants.NOT_DELETED
                )) {

            throw new ConflictException(
                    OrganizationMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!isBlank(request.getEmail())) {

            String email =
                    normalizeEmail(
                            request.getEmail()
                    );

            if (organizationRepository
                    .existsByEmailAndIsDeleted(
                            email,
                            OrganizationConstants.NOT_DELETED
                    )) {

                throw new ConflictException(
                        OrganizationMessages.EMAIL_ALREADY_EXISTS
                );
            }
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateOrganizationRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Organization update request cannot be null."
            );
        }

        if (isBlank(request.getPublicId())) {
            throw new BadRequestException(
                    OrganizationMessages.PUBLIC_ID_REQUIRED
            );
        }

        if (isBlank(request.getOrganizationCode())) {
            throw new BadRequestException(
                    "Organization code is required."
            );
        }

        if (isBlank(request.getOrganizationName())) {
            throw new BadRequestException(
                    "Organization name is required."
            );
        }

        if (isBlank(request.getOrganizationTypePublicId())) {
            throw new BadRequestException(
                    OrganizationMessages.TYPE_REQUIRED
            );
        }

        if (isBlank(request.getOrganizationStatusPublicId())) {
            throw new BadRequestException(
                    OrganizationMessages.STATUS_REQUIRED
            );
        }

        String publicId =
                request.getPublicId().trim();

        String code =
                normalizeCode(
                        request.getOrganizationCode()
                );

        validateAndGet(publicId);

        if (organizationRepository
                .existsByOrganizationCodeAndIsDeletedAndPublicIdNot(
                        code,
                        OrganizationConstants.NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    OrganizationMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!isBlank(request.getEmail())) {

            String email =
                    normalizeEmail(
                            request.getEmail()
                    );

            if (organizationRepository
                    .existsByEmailAndIsDeletedAndPublicIdNot(
                            email,
                            OrganizationConstants.NOT_DELETED,
                            publicId
                    )) {

                throw new ConflictException(
                        OrganizationMessages.EMAIL_ALREADY_EXISTS
                );
            }
        }
    }


    // =========================================================
    // FIND
    // =========================================================

    public Organization validateAndGet(
            String publicId) {

        if (isBlank(publicId)) {
            throw new BadRequestException(
                    OrganizationMessages.PUBLIC_ID_REQUIRED
            );
        }

        return organizationRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        OrganizationConstants.NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                OrganizationMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalizeCode(
            String code) {

        return code.trim().toUpperCase();
    }

    private String normalizeEmail(
            String email) {

        return email.trim().toLowerCase();
    }

    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
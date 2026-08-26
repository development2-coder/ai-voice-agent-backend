package com.infinitio.aivoiceplatform.master.platformmodule.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.platformmodule.constant.PlatformModuleMessages;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.CreatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.UpdatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.entity.PlatformModule;
import com.infinitio.aivoiceplatform.master.platformmodule.repository.PlatformModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for Platform Module.
 *
 * Performs business validations for:
 * - Create
 * - Update
 * - Get
 * - Duplicate module code
 * - Duplicate module name
 * - Duplicate route
 *
 * Soft deleted records are ignored during validation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformModuleValidator {

    private static final Integer NOT_DELETED = 0;

    private final PlatformModuleRepository platformModuleRepository;


    // =========================================================
    // CREATE VALIDATION
    // =========================================================

    /**
     * Validate Platform Module create request.
     *
     * Checks:
     * - Module code uniqueness
     * - Module name uniqueness
     * - Route uniqueness
     *
     * @param request Create request
     */
    public void validateForCreate(
            CreatePlatformModuleRequest request) {

        log.info(
                "Validating Platform Module create request."
        );

        if (request == null) {

            throw new IllegalArgumentException(
                    "Platform Module request is required."
            );
        }

        String moduleCode =
                normalize(
                        request.getModuleCode()
                );

        String moduleName =
                normalize(
                        request.getModuleName()
                );

        String route =
                normalize(
                        request.getRoute()
                );


        // =====================================================
        // MODULE CODE
        // =====================================================

        if (platformModuleRepository
                .existsByModuleCodeAndIsDeleted(
                        moduleCode,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    PlatformModuleMessages
                            .MODULE_CODE_ALREADY_EXISTS
            );
        }


        // =====================================================
        // MODULE NAME
        // =====================================================

        if (platformModuleRepository
                .existsByModuleNameAndIsDeleted(
                        moduleName,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    PlatformModuleMessages
                            .MODULE_NAME_ALREADY_EXISTS
            );
        }


        // =====================================================
        // ROUTE
        // =====================================================

        if (route != null
                && platformModuleRepository
                .existsByRouteAndIsDeleted(
                        route,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    PlatformModuleMessages
                            .MODULE_ROUTE_ALREADY_EXISTS
            );
        }

        log.info(
                "Platform Module create validation completed."
        );
    }


    // =========================================================
    // UPDATE VALIDATION
    // =========================================================

    /**
     * Validate Platform Module update request.
     *
     * Checks:
     * - Existing module
     * - Module code uniqueness
     * - Module name uniqueness
     * - Route uniqueness
     *
     * The current module itself is excluded from
     * duplicate checks.
     *
     * @param request Update request
     */
    public void validateForUpdate(
            UpdatePlatformModuleRequest request) {

        log.info(
                "Validating Platform Module update request."
        );

        if (request == null) {

            throw new IllegalArgumentException(
                    "Platform Module update request is required."
            );
        }

        PlatformModule existingModule =
                validateAndGet(
                        request.getPublicId()
                );


        String moduleCode =
                normalize(
                        request.getModuleCode()
                );

        String moduleName =
                normalize(
                        request.getModuleName()
                );

        String route =
                normalize(
                        request.getRoute()
                );


        // =====================================================
        // MODULE CODE
        // =====================================================

        if (!equalsIgnoreCase(
                existingModule.getModuleCode(),
                moduleCode
        )) {

            if (platformModuleRepository
                    .existsByModuleCodeAndIsDeleted(
                            moduleCode,
                            NOT_DELETED
                    )) {

                throw new ConflictException(
                        PlatformModuleMessages
                                .MODULE_CODE_ALREADY_EXISTS
                );
            }
        }


        // =====================================================
        // MODULE NAME
        // =====================================================

        if (!equalsIgnoreCase(
                existingModule.getModuleName(),
                moduleName
        )) {

            if (platformModuleRepository
                    .existsByModuleNameAndIsDeleted(
                            moduleName,
                            NOT_DELETED
                    )) {

                throw new ConflictException(
                        PlatformModuleMessages
                                .MODULE_NAME_ALREADY_EXISTS
                );
            }
        }


        // =====================================================
        // ROUTE
        // =====================================================

        String existingRoute =
                normalize(
                        existingModule.getRoute()
                );

        if (!equalsIgnoreCase(
                existingRoute,
                route
        )) {

            if (route != null
                    && platformModuleRepository
                    .existsByRouteAndIsDeleted(
                            route,
                            NOT_DELETED
                    )) {

                throw new ConflictException(
                        PlatformModuleMessages
                                .MODULE_ROUTE_ALREADY_EXISTS
                );
            }
        }

        log.info(
                "Platform Module update validation completed."
        );
    }


    // =========================================================
    // GET / FIND
    // =========================================================

    /**
     * Find a non-deleted Platform Module by public ID.
     *
     * @param publicId Public UUID
     * @return PlatformModule
     */
    public PlatformModule validateAndGet(
            String publicId) {

        log.info(
                "Validating Platform Module Public Id : {}",
                publicId
        );

        String normalizedPublicId =
                normalize(
                        publicId
                );

        if (normalizedPublicId == null) {

            throw new ResourceNotFoundException(
                    PlatformModuleMessages
                            .MODULE_NOT_FOUND
            );
        }

        return platformModuleRepository
                .findByPublicIdAndIsDeleted(
                        normalizedPublicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PlatformModuleMessages
                                        .MODULE_NOT_FOUND
                        )
                );
    }


    // =========================================================
    // NORMALIZE STRING
    // =========================================================

    /**
     * Trim a string.
     *
     * Blank values are converted to null.
     *
     * @param value input value
     * @return normalized value
     */
    private String normalize(
            String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }


    // =========================================================
    // CASE-INSENSITIVE COMPARISON
    // =========================================================

    /**
     * Compare two strings ignoring case.
     *
     * Null-safe.
     *
     * @param first first value
     * @param second second value
     * @return true when values are equal
     */
    private boolean equalsIgnoreCase(
            String first,
            String second) {

        if (first == null
                && second == null) {

            return true;
        }

        if (first == null
                || second == null) {

            return false;
        }

        return first.equalsIgnoreCase(
                second
        );
    }
}
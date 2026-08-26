package com.infinitio.aivoiceplatform.user.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.user.constant.UserMessages;
import com.infinitio.aivoiceplatform.user.dto.request.CreateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.request.UpdateUserRequest;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * User Validator.
 *
 * Handles User request and business validations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidator {

    private static final Integer NOT_DELETED = 0;

    private final UserRepository userRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateUserRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "User request cannot be null."
            );
        }

        validateRequiredFields(
                request.getTenantPublicId(),
                request.getOrganizationPublicId(),
                request.getRolePublicId(),
                request.getUsername(),
                request.getEmail()
        );

        validatePassword(
                request.getPassword()
        );

        String username =
                normalize(
                        request.getUsername()
                );

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        if (userRepository
                .existsByUsernameAndIsDeleted(
                        username,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    UserMessages.USERNAME_ALREADY_EXISTS
            );
        }

        if (userRepository
                .existsByEmailAndIsDeleted(
                        email,
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    UserMessages.EMAIL_ALREADY_EXISTS
            );
        }

        /*
         * =====================================================
         * FIRST USER RULE
         * =====================================================
         *
         * If there are no users yet, the first user MUST be
         * SUPER_ADMIN.
         *
         * We validate the role itself later through RoleValidator.
         */
        boolean anyUserExists =
                userRepository.existsByIsDeleted(
                        NOT_DELETED
                );

        if (!anyUserExists) {

            /*
             * We cannot know the role name directly from the
             * request because the request contains rolePublicId.
             *
             * The service will resolve the Role and perform
             * the final first-user role validation.
             */

            log.info(
                    "No active users found. First user must be SUPER_ADMIN."
            );
        }

        /*
         * =====================================================
         * SUPER ADMIN DUPLICATE RULE
         * =====================================================
         *
         * The actual role is resolved in UserServiceImpl.
         */
    }


    // =========================================================
    // FIRST USER
    // =========================================================

    public boolean isFirstUser() {

        return !userRepository.existsByIsDeleted(
                NOT_DELETED
        );
    }


    // =========================================================
    // SUPER ADMIN EXISTS
    // =========================================================

    public boolean isSuperAdminAlreadyExists() {

        return userRepository
                .existsByRole_RoleCodeAndIsDeleted(
                        RoleConstants.SUPER_ADMIN,
                        NOT_DELETED
                );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateUserRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "User update request cannot be null."
            );
        }

        if (isBlank(request.getPublicId())) {

            throw new BadRequestException(
                    "User Public ID is required."
            );
        }

        validateRequiredFields(
                request.getTenantPublicId(),
                request.getOrganizationPublicId(),
                request.getRolePublicId(),
                request.getUsername(),
                request.getEmail()
        );

        if (!isBlank(request.getPassword())) {

            validatePassword(
                    request.getPassword()
            );
        }

        String publicId =
                request.getPublicId().trim();

        validateAndGet(
                publicId
        );

        String username =
                normalize(
                        request.getUsername()
                );

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        if (userRepository
                .existsByUsernameAndIsDeletedAndPublicIdNot(
                        username,
                        NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    UserMessages.USERNAME_ALREADY_EXISTS
            );
        }

        if (userRepository
                .existsByEmailAndIsDeletedAndPublicIdNot(
                        email,
                        NOT_DELETED,
                        publicId
                )) {

            throw new ConflictException(
                    UserMessages.EMAIL_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // FIND USER
    // =========================================================

    public User validateAndGet(
            String publicId) {

        if (isBlank(publicId)) {

            throw new BadRequestException(
                    "User Public ID is required."
            );
        }

        return userRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                UserMessages.USER_NOT_FOUND
                        )
                );
    }


    // =========================================================
    // REQUIRED FIELDS
    // =========================================================

    private void validateRequiredFields(
            String tenantPublicId,
            String organizationPublicId,
            String rolePublicId,
            String username,
            String email) {

        if (isBlank(tenantPublicId)) {

            throw new BadRequestException(
                    "Tenant is required."
            );
        }

        if (isBlank(organizationPublicId)) {

            throw new BadRequestException(
                    "Organization is required."
            );
        }

        if (isBlank(rolePublicId)) {

            throw new BadRequestException(
                    "Role is required."
            );
        }

        if (isBlank(username)) {

            throw new BadRequestException(
                    "Username is required."
            );
        }

        if (isBlank(email)) {

            throw new BadRequestException(
                    "Email is required."
            );
        }
    }


    // =========================================================
    // PASSWORD
    // =========================================================

    private void validatePassword(
            String password) {

        if (isBlank(password)) {

            throw new BadRequestException(
                    "Password is required."
            );
        }

        if (password.length() < 8) {

            throw new BadRequestException(
                    "Password must contain at least 8 characters."
            );
        }
    }


    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalize(
            String value) {

        return value.trim();
    }


    private String normalizeEmail(
            String value) {

        return value.trim().toLowerCase();
    }


    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
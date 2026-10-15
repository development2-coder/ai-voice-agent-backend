package com.infinitio.aivoiceplatform.master.usermenu.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.menu.entity.Menu;
import com.infinitio.aivoiceplatform.master.menu.validator.MenuValidator;
import com.infinitio.aivoiceplatform.master.usermenu.constant.UserMenuConstants;
import com.infinitio.aivoiceplatform.master.usermenu.constant.UserMenuMessages;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.CreateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.UpdateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.entity.UserMenu;
import com.infinitio.aivoiceplatform.master.usermenu.repository.UserMenuRepository;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for User Menu operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMenuValidator {

    private static final Integer NOT_DELETED =
            UserMenuConstants.NOT_DELETED;

    private final UserMenuRepository userMenuRepository;

    private final UserRepository userRepository;

    private final MenuValidator menuValidator;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateUserMenuRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "User menu request is required."
            );
        }

        if (!hasText(
                request.getUserPublicId()
        )) {

            throw new BadRequestException(
                    "User public ID is required."
            );
        }

        if (!hasText(
                request.getMenuPublicId()
        )) {

            throw new BadRequestException(
                    "Menu public ID is required."
            );
        }

        validateVisibility(
                request.getIsVisible()
        );

        User user =
                getUser(
                        request.getUserPublicId()
                );

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId()
                                .trim()
                );

        if (userMenuRepository
                .existsByUserIdAndMenuIdAndIsDeleted(
                        user.getId(),
                        menu.getId(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    UserMenuMessages.ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateUserMenuRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "User menu request is required."
            );
        }

        if (!hasText(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "User menu public ID is required."
            );
        }

        if (!hasText(
                request.getUserPublicId()
        )) {

            throw new BadRequestException(
                    "User public ID is required."
            );
        }

        if (!hasText(
                request.getMenuPublicId()
        )) {

            throw new BadRequestException(
                    "Menu public ID is required."
            );
        }

        validateVisibility(
                request.getIsVisible()
        );

        UserMenu existing =
                validateAndGet(
                        request.getPublicId()
                );

        User user =
                getUser(
                        request.getUserPublicId()
                );

        Menu menu =
                menuValidator.validateAndGet(
                        request.getMenuPublicId()
                                .trim()
                );

        if (userMenuRepository
                .existsByUserIdAndMenuIdAndIsDeletedAndIdNot(
                        user.getId(),
                        menu.getId(),
                        NOT_DELETED,
                        existing.getId()
                )) {

            throw new ConflictException(
                    UserMenuMessages.ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // GET
    // =========================================================

    public UserMenu validateAndGet(
            String publicId) {

        if (!hasText(publicId)) {

            throw new BadRequestException(
                    "User menu public ID is required."
            );
        }

        return userMenuRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                UserMenuMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // USER
    // =========================================================

    private User getUser(
            String publicId) {

        return userRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                UserMenuMessages.INVALID_USER
                        )
                );
    }


    // =========================================================
    // VISIBILITY
    // =========================================================

    private void validateVisibility(
            Integer isVisible) {

        if (isVisible == null) {
            return;
        }

        if (!UserMenuConstants.VISIBLE.equals(
                isVisible
        )
                && !UserMenuConstants.HIDDEN.equals(
                isVisible
        )) {

            throw new BadRequestException(
                    UserMenuMessages.INVALID_VISIBILITY
            );
        }
    }


    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}
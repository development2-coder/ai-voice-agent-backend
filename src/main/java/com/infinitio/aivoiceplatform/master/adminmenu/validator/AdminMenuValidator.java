package com.infinitio.aivoiceplatform.master.adminmenu.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.master.adminmenu.constant.AdminMenuMessages;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.CreateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.UpdateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.entity.AdminMenu;
import com.infinitio.aivoiceplatform.master.adminmenu.repository.AdminMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.infinitio.aivoiceplatform.master.adminmenu.constant.AdminMenuConstants.NOT_DELETED;

/**
 * Validator for AdminMenu.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AdminMenuValidator {

    private final AdminMenuRepository adminMenuRepository;

    public void validateForCreate(
            CreateAdminMenuRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Admin menu request is required."
            );
        }

        validateVisibility(
                request.getIsVisible()
        );
    }

    public void validateForUpdate(
            UpdateAdminMenuRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Admin menu update request is required."
            );
        }

        validateVisibility(
                request.getIsVisible()
        );

        AdminMenu existing =
                validateAndGet(
                        request.getPublicId()
                );

        /*
         * Prevent duplicate ADMIN + Menu assignment.
         */
        if (adminMenuRepository
                .existsByAdmin_IdAndMenu_IdAndIsDeletedAndIdNot(
                        existing.getAdmin().getId(),
                        existing.getMenu().getId(),
                        NOT_DELETED,
                        existing.getId()
                )) {

            throw new ConflictException(
                    AdminMenuMessages.ALREADY_EXISTS
            );
        }
    }

    public AdminMenu validateAndGet(
            String publicId) {

        return adminMenuRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                AdminMenuMessages.NOT_FOUND
                        )
                );
    }

    private void validateVisibility(
            Integer isVisible) {

        if (isVisible != null
                && !Integer.valueOf(0).equals(isVisible)
                && !Integer.valueOf(1).equals(isVisible)) {

            throw new BadRequestException(
                    AdminMenuMessages.INVALID_VISIBILITY
            );
        }
    }
}
package com.infinitio.aivoiceplatform.user.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.user.constant.UserMessages;
import com.infinitio.aivoiceplatform.user.dto.request.CreateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.request.UpdateUserRequest;
import com.infinitio.aivoiceplatform.user.dto.response.UserResponse;
import com.infinitio.aivoiceplatform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller.
 *
 * <p>
 * Provides REST APIs for user management.
 * </p>
 *
 * <p>
 * Authorization is handled through the common library RBAC
 * permission framework. Tenant-level access is additionally
 * enforced inside the UserService implementation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Creates a user.
     *
     * <p>
     * RBAC annotation is intentionally not applied here because
     * the application supports creation of the first SUPER_ADMIN
     * before authentication is available.
     *
     * <p>
     * After bootstrap, UserServiceImpl performs the required
     * SUPER_ADMIN / ADMIN / USER authorization checks.
     *
     * @param request create user request
     * @return created user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response =
                userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                UserMessages.USER_CREATED,
                                response
                        )
                );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates a user.
     *
     * @param request update user request
     * @return updated user
     */
    @PutMapping
    @PreAuthorize("hasPermission(null, 'USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response =
                userService.update(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        UserMessages.USER_UPDATED,
                        response
                )
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Gets a user by public ID.
     *
     * @param publicId user public ID
     * @return user details
     */
    @GetMapping("/{publicId}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> getByPublicId(
            @PathVariable String publicId) {

        UserResponse response =
                userService.getByPublicId(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully.",
                        response
                )
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Gets users visible to the authenticated user.
     *
     * <p>
     * SUPER_ADMIN receives users across all tenants.
     * ADMIN receives users from the current tenant only.
     *
     * @param page page number
     * @param size page size
     * @return users
     */
    @GetMapping
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<
            ApiResponse<PageResponse<UserResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<UserResponse> response =
                userService.getAll(
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully.",
                        response
                )
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Soft deletes a user.
     *
     * @param publicId user public ID
     * @return success response
     */
    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasPermission(null, 'USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        userService.delete(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        UserMessages.USER_DELETED,
                        null
                )
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activates a user.
     *
     * @param publicId user public ID
     * @return success response
     */
    @PatchMapping("/{publicId}/activate")
    @PreAuthorize("hasPermission(null, 'USER_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        userService.activate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User activated successfully.",
                        null
                )
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivates a user.
     *
     * @param publicId user public ID
     * @return success response
     */
    @PatchMapping("/{publicId}/deactivate")
    @PreAuthorize("hasPermission(null, 'USER_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        userService.deactivate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User deactivated successfully.",
                        null
                )
        );
    }
}
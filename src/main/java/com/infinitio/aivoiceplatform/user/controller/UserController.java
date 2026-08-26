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
import org.springframework.web.bind.annotation.*;

/**
 * User Controller.
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

    @PutMapping
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

    @GetMapping("/{publicId}")
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

    @GetMapping
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

    @DeleteMapping("/{publicId}")
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

    @PatchMapping("/{publicId}/activate")
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

    @PatchMapping("/{publicId}/deactivate")
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
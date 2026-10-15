package com.infinitio.aivoiceplatform.phonenumber.controller;

import com.infi.library.annotation.RequiresPermission;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.phonenumber.constant.PhoneNumberMessages;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.CreatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.UpdatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.response.PhoneNumberResponse;
import com.infinitio.aivoiceplatform.phonenumber.service.PhoneNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for phone-number management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/phone-numbers")
@Tag(
        name = "Phone Number",
        description = "AI Voice Phone Number Management APIs"
)
public class PhoneNumberController {

    private final PhoneNumberService phoneNumberService;

    @Operation(summary = "Create Phone Number")
    @PostMapping
    @RequiresPermission("PHONE_NUMBER_CREATE")
    public ResponseEntity<ApiResponse<PhoneNumberResponse>> create(
            @Valid @RequestBody CreatePhoneNumberRequest request) {

        PhoneNumberResponse response =
                phoneNumberService.create(request);

        return ResponseBuilder.created(
                response,
                PhoneNumberMessages.CREATED
        );
    }

    @Operation(summary = "Update Phone Number")
    @PutMapping
    @RequiresPermission("PHONE_NUMBER_UPDATE")
    public ResponseEntity<ApiResponse<PhoneNumberResponse>> update(
            @Valid @RequestBody UpdatePhoneNumberRequest request) {

        PhoneNumberResponse response =
                phoneNumberService.update(request);

        return ResponseBuilder.success(
                response,
                PhoneNumberMessages.UPDATED
        );
    }

    @Operation(summary = "Get Phone Number By Public Id")
    @GetMapping("/{publicId}")
    @RequiresPermission("PHONE_NUMBER_VIEW")
    public ResponseEntity<ApiResponse<PhoneNumberResponse>> getByPublicId(
            @PathVariable String publicId) {

        PhoneNumberResponse response =
                phoneNumberService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Phone number fetched successfully."
        );
    }

    @Operation(summary = "Get All Phone Numbers")
    @GetMapping
    @RequiresPermission("PHONE_NUMBER_VIEW")
    public ResponseEntity<ApiResponse<PageResponse<PhoneNumberResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        PageResponse<PhoneNumberResponse> response =
                phoneNumberService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Phone numbers fetched successfully."
        );
    }

    @Operation(summary = "Delete Phone Number")
    @DeleteMapping("/{publicId}")
    @RequiresPermission("PHONE_NUMBER_DELETE")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        phoneNumberService.delete(publicId);

        return ResponseBuilder.success(
                null,
                PhoneNumberMessages.DELETED
        );
    }

    @Operation(summary = "Activate Phone Number")
    @PatchMapping("/{publicId}/activate")
    @RequiresPermission("PHONE_NUMBER_UPDATE")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        phoneNumberService.activate(publicId);

        return ResponseBuilder.success(
                null,
                PhoneNumberMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Phone Number")
    @PatchMapping("/{publicId}/deactivate")
    @RequiresPermission("PHONE_NUMBER_UPDATE")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        phoneNumberService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                PhoneNumberMessages.DEACTIVATED
        );
    }
}
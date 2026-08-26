package com.infinitio.aivoiceplatform.master.platformmodule.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.CreatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.UpdatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.response.PlatformModuleResponse;

/**
 * Service interface for Platform Module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface PlatformModuleService {

    /**
     * Create Platform Module.
     *
     * @param request Create Request
     * @return PlatformModuleResponse
     */
    PlatformModuleResponse create(CreatePlatformModuleRequest request);

    /**
     * Update Platform Module.
     *
     * @param request Update Request
     * @return PlatformModuleResponse
     */
    PlatformModuleResponse update(UpdatePlatformModuleRequest request);

    /**
     * Get Platform Module by Public Id.
     *
     * @param publicId Public Id
     * @return PlatformModuleResponse
     */
    PlatformModuleResponse getByPublicId(String publicId);

    /**
     * Get All Platform Modules.
     *
     * @param page Page Number
     * @param size Page Size
     * @return PageResponse
     */
    PageResponse<PlatformModuleResponse> getAll(int page, int size);

    /**
     * Delete Platform Module.
     *
     * @param publicId Public Id
     */
    void delete(String publicId);

    /**
     * Activate Platform Module.
     *
     * @param publicId Public Id
     */
    void activate(String publicId);

    /**
     * Deactivate Platform Module.
     *
     * @param publicId Public Id
     */
    void deactivate(String publicId);

}
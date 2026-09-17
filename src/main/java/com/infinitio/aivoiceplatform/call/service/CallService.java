package com.infinitio.aivoiceplatform.call.service;

import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;

import java.util.List;

/**
 * Service interface for Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallService {

    /**
     * Creates a new call.
     *
     * @param request call creation request
     * @return created call
     */
    CallResponse create(
            CreateCallRequest request
    );

    /**
     * Updates an existing call.
     *
     * @param request call update request
     * @return updated call
     */
    CallResponse update(
            UpdateCallRequest request
    );

    /**
     * Fetches a call by public identifier.
     *
     * @param publicId call public identifier
     * @return call response
     */
    CallResponse getByPublicId(
            String publicId
    );

    /**
     * Fetches calls according to the current user's role.
     *
     * <p>
     * SUPER_ADMIN receives calls from all tenants.
     * Other authenticated users receive calls belonging
     * only to their own tenant.
     * </p>
     *
     * @return accessible calls
     */
    List<CallResponse> getAll();

    /**
     * Fetches all calls belonging to a specific tenant.
     *
     * <p>
     * This operation is restricted to SUPER_ADMIN.
     * </p>
     *
     * @param tenantId tenant database identifier
     * @return tenant calls
     */
    List<CallResponse> getByTenantId(
            Long tenantId
    );

    /**
     * Fetches all calls associated with a campaign contact.
     *
     * @param campaignContactPublicId campaign contact public identifier
     * @return campaign contact call history
     */
    List<CallResponse> getByCampaignContact(
            String campaignContactPublicId
    );

    /**
     * Soft deletes a call.
     *
     * @param publicId call public identifier
     */
    void delete(
            String publicId
    );

    /**
     * Activates a call.
     *
     * @param publicId call public identifier
     */
    void activate(
            String publicId
    );

    /**
     * Deactivates a call.
     *
     * @param publicId call public identifier
     */
    void deactivate(
            String publicId
    );
}
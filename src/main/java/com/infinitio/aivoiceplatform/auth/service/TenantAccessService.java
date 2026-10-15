package com.infinitio.aivoiceplatform.auth.service;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Central tenant and current-user access validation service
 * for the AI Voice Platform.
 *
 * <p>
 * This service is application-specific and intentionally remains
 * outside the reusable Infinitio common library.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class TenantAccessService {

    private final CurrentUserService currentUserService;


    // =========================================================
    // CURRENT USER
    // =========================================================

    /**
     * Returns the current authenticated user's database ID.
     *
     * @return current user ID
     */
    public Long getCurrentUserId() {
        return currentUserService.getCurrentUserId();
    }


    /**
     * Returns the current authenticated user's public ID.
     *
     * @return current user public ID
     */
    public String getCurrentUserPublicId() {
        return currentUserService.getCurrentUserPublicId();
    }


    /**
     * Returns the current user's email.
     *
     * @return current user email
     */
    public String getCurrentUserEmail() {
        return currentUserService.getCurrentUserEmail();
    }


    /**
     * Returns the current user's tenant ID.
     *
     * @return current tenant ID
     */
    public Long getCurrentTenantId() {
        return currentUserService.getCurrentTenantId();
    }


    /**
     * Checks whether the current user is authenticated.
     *
     * @return true when authenticated
     */
    public boolean isAuthenticated() {
        return currentUserService.isAuthenticated();
    }


    /**
     * Checks whether the current user is SUPER_ADMIN.
     *
     * @return true when current user is SUPER_ADMIN
     */
    public boolean isSuperAdmin() {
        return currentUserService.isSuperAdmin();
    }


    /**
     * Checks whether the current user is ADMIN.
     *
     * @return true when current user is ADMIN
     */
    public boolean isAdmin() {
        return currentUserService.isAdmin();
    }


    /**
     * Checks whether the current user is USER.
     *
     * @return true when current user is USER
     */
    public boolean isUser() {
        return currentUserService.isUser();
    }


    // =========================================================
    // TENANT ACCESS
    // =========================================================

    /**
     * Validates access to a tenant.
     *
     * @param tenant tenant
     */
    public void validateTenantAccess(
            Tenant tenant) {

        if (isSuperAdmin()) {
            return;
        }

        if (tenant == null
                || tenant.getId() == null) {

            throw new ForbiddenException(
                    "Tenant information is unavailable."
            );
        }

        Long currentTenantId =
                getCurrentTenantId();

        if (!tenant.getId().equals(currentTenantId)) {

            throw new ForbiddenException(
                    "You do not have access to this tenant."
            );
        }
    }


    /**
     * Validates access using tenant ID.
     *
     * @param tenantId tenant ID
     */
    public void validateTenantAccess(
            Long tenantId) {

        if (isSuperAdmin()) {
            return;
        }

        if (tenantId == null
                || !tenantId.equals(
                getCurrentTenantId()
        )) {

            throw new ForbiddenException(
                    "You do not have access to this tenant."
            );
        }
    }


    // =========================================================
    // ENTITY ACCESS
    // =========================================================

    /**
     * Validates access to an Agent and its tenant.
     *
     * @param agent agent
     */
    public void validateAgentAccess(
            Agent agent) {

        if (agent == null) {

            throw new ForbiddenException(
                    "Agent information is unavailable."
            );
        }

        validateTenantAccess(
                agent.getTenant()
        );
    }


    /**
     * Validates access to a Campaign and its tenant.
     *
     * @param campaign campaign
     */
    public void validateCampaignAccess(
            Campaign campaign) {

        if (campaign == null
                || campaign.getAgent() == null) {

            throw new ForbiddenException(
                    "Campaign tenant information is unavailable."
            );
        }

        validateAgentAccess(
                campaign.getAgent()
        );
    }


    /**
     * Validates access to a Knowledge Base and its tenant.
     *
     * @param knowledgeBase knowledge base
     */
    public void validateKnowledgeBaseAccess(
            KnowledgeBase knowledgeBase) {

        if (knowledgeBase == null
                || knowledgeBase.getAgent() == null) {

            throw new ForbiddenException(
                    "Knowledge Base tenant information is unavailable."
            );
        }

        validateAgentAccess(
                knowledgeBase.getAgent()
        );
    }


    /**
     * Validates access to a Flow and its tenant.
     *
     * <p>
     * Tenant relationship:
     *
     * <pre>
     * Flow
     *   ↓
     * Agent
     *   ↓
     * Tenant
     * </pre>
     *
     * SUPER_ADMIN can access flows across all tenants.
     * ADMIN and USER can access only flows belonging
     * to their current tenant.
     *
     * @param flow flow
     */
    public void validateFlowAccess(
            Flow flow) {

        if (flow == null) {

            throw new ForbiddenException(
                    "Flow information is unavailable."
            );
        }

        if (flow.getAgent() == null) {

            throw new ForbiddenException(
                    "Flow tenant information is unavailable."
            );
        }

        validateAgentAccess(
                flow.getAgent()
        );
    }


    // =========================================================
    // PLATFORM ACCESS
    // =========================================================

    /**
     * Validates organization management access.
     *
     * <p>
     * Organization management is restricted to SUPER_ADMIN.
     * </p>
     */
    public void validateOrganizationManagementAccess() {

        if (!isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required."
            );
        }

        if (!isSuperAdmin()) {

            throw new ForbiddenException(
                    "Only SUPER_ADMIN can manage organizations."
            );
        }
    }


    /**
     * Validates SUPER_ADMIN access.
     */
    public void validateSuperAdminAccess() {

        if (!isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required."
            );
        }

        if (!isSuperAdmin()) {

            throw new ForbiddenException(
                    "Only SUPER_ADMIN can perform this operation."
            );
        }
    }
}
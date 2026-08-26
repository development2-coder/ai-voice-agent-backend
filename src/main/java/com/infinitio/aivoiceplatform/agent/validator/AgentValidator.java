package com.infinitio.aivoiceplatform.agent.validator;

import com.infinitio.aivoiceplatform.agent.dto.request.CreateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.request.UpdateAgentRequest;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.repository.AgentRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentValidator {

    private static final Integer NOT_DELETED = 0;

    private final AgentRepository agentRepository;


    // =========================================================
    // CREATE VALIDATION
    // =========================================================

    public void validateCreate(
            CreateAgentRequest request,
            Long tenantId) {

        if (request == null) {
            throw new BadRequestException(
                    "Agent request cannot be null."
            );
        }

        if (tenantId == null) {
            throw new BadRequestException(
                    "Tenant is required."
            );
        }

        validateAgentCode(
                request.getAgentCode()
        );

        validateAgentName(
                request.getAgentName()
        );

        String agentCode =
                request.getAgentCode().trim();

        String agentName =
                request.getAgentName().trim();

        if (agentRepository
                .existsByAgentCodeAndTenantIdAndIsDeleted(
                        agentCode,
                        tenantId,
                        NOT_DELETED
                )) {

            throw new BadRequestException(
                    "Agent code already exists for this tenant."
            );
        }

        if (agentRepository
                .existsByAgentNameAndTenantIdAndIsDeleted(
                        agentName,
                        tenantId,
                        NOT_DELETED
                )) {

            throw new BadRequestException(
                    "Agent name already exists for this tenant."
            );
        }
    }


    // =========================================================
    // UPDATE VALIDATION
    // =========================================================

    public void validateUpdate(
            UpdateAgentRequest request,
            Long tenantId,
            String currentPublicId) {

        if (request == null) {
            throw new BadRequestException(
                    "Agent request cannot be null."
            );
        }

        if (tenantId == null) {
            throw new BadRequestException(
                    "Tenant is required."
            );
        }

        if (currentPublicId == null
                || currentPublicId.isBlank()) {

            throw new BadRequestException(
                    "Agent public ID is required."
            );
        }

        if (request.getAgentCode() != null
                && !request.getAgentCode().isBlank()) {

            validateAgentCode(
                    request.getAgentCode()
            );

            String agentCode =
                    request.getAgentCode().trim();

            if (agentRepository
                    .existsByAgentCodeAndTenantIdAndIsDeletedAndPublicIdNot(
                            agentCode,
                            tenantId,
                            NOT_DELETED,
                            currentPublicId
                    )) {

                throw new BadRequestException(
                        "Agent code already exists for this tenant."
                );
            }
        }

        if (request.getAgentName() != null
                && !request.getAgentName().isBlank()) {

            validateAgentName(
                    request.getAgentName()
            );

            String agentName =
                    request.getAgentName().trim();

            if (agentRepository
                    .existsByAgentNameAndTenantIdAndIsDeletedAndPublicIdNot(
                            agentName,
                            tenantId,
                            NOT_DELETED,
                            currentPublicId
                    )) {

                throw new BadRequestException(
                        "Agent name already exists for this tenant."
                );
            }
        }
    }


    // =========================================================
    // GET / VALIDATE AND GET
    // =========================================================

    public Agent validateAndGet(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "Agent public ID is required."
            );
        }

        return agentRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agent not found."
                        )
                );
    }


    // =========================================================
    // CODE VALIDATION
    // =========================================================

    private void validateAgentCode(
            String agentCode) {

        if (agentCode == null
                || agentCode.isBlank()) {

            throw new BadRequestException(
                    "Agent code is required."
            );
        }

        String code =
                agentCode.trim();

        if (code.length() < 2
                || code.length() > 50) {

            throw new BadRequestException(
                    "Agent code must be between 2 and 50 characters."
            );
        }

        if (!code.matches(
                "^[A-Za-z0-9_-]+$"
        )) {

            throw new BadRequestException(
                    "Agent code may contain only letters, "
                            + "numbers, underscore and hyphen."
            );
        }
    }


    // =========================================================
    // NAME VALIDATION
    // =========================================================

    private void validateAgentName(
            String agentName) {

        if (agentName == null
                || agentName.isBlank()) {

            throw new BadRequestException(
                    "Agent name is required."
            );
        }

        String name =
                agentName.trim();

        if (name.length() < 2
                || name.length() > 100) {

            throw new BadRequestException(
                    "Agent name must be between 2 and 100 characters."
            );
        }
    }
}
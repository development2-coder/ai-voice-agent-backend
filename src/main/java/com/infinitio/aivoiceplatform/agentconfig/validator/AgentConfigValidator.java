package com.infinitio.aivoiceplatform.agentconfig.validator;

import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigMessages;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.CreateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.UpdateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.entity.AgentConfig;
import com.infinitio.aivoiceplatform.agentconfig.repository.AgentConfigRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Agent Configuration Validator.
 *
 * Handles Agent Configuration request and business validations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentConfigValidator {

    private static final Integer NOT_DELETED = 0;

    private final AgentConfigRepository agentConfigRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateAgentConfigRequest request,
            Long agentId) {

        if (request == null) {

            throw new BadRequestException(
                    "Agent configuration request cannot be null."
            );
        }

        if (agentId == null) {

            throw new BadRequestException(
                    "Agent is required."
            );
        }

        /*
         * An Agent can have only one configuration.
         *
         * The database also has a unique constraint on agent_id,
         * therefore this check must happen before save.
         */
        if (agentConfigRepository.existsByAgentId(
                agentId
        )) {

            throw new ConflictException(
                    AgentConfigMessages.CONFIG_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateAgentConfigRequest request,
            Long agentId) {

        if (request == null) {

            throw new BadRequestException(
                    "Agent configuration request cannot be null."
            );
        }

        if (isBlank(
                request.getPublicId()
        )) {

            throw new BadRequestException(
                    "Agent configuration public ID is required."
            );
        }

        if (agentId == null) {

            throw new BadRequestException(
                    "Agent is required."
            );
        }

        AgentConfig existingConfig =
                validateAndGet(
                        request.getPublicId()
                );

        /*
         * If the configuration is moved to another Agent,
         * make sure the target Agent does not already have
         * a configuration.
         */
        if (!existingConfig
                .getAgent()
                .getId()
                .equals(agentId)
                && agentConfigRepository
                .existsByAgentId(agentId)) {

            throw new ConflictException(
                    AgentConfigMessages.CONFIG_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // GET
    // =========================================================

    public AgentConfig validateAndGet(
            String publicId) {

        if (isBlank(publicId)) {

            throw new BadRequestException(
                    "Agent configuration public ID is required."
            );
        }

        return agentConfigRepository
                .findByPublicIdAndIsDeleted(
                        publicId.trim(),
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                AgentConfigMessages.NOT_FOUND
                        )
                );
    }


    // =========================================================
    // VALIDATE PAGE
    // =========================================================

    public void validatePagination(
            int page,
            int size) {

        if (page < 0) {

            throw new BadRequestException(
                    "Page cannot be negative."
            );
        }

        if (size <= 0) {

            throw new BadRequestException(
                    "Size must be greater than zero."
            );
        }
    }


    // =========================================================
    // UTILITY
    // =========================================================

    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
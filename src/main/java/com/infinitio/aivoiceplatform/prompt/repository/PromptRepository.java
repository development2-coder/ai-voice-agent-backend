package com.infinitio.aivoiceplatform.prompt.repository;

import com.infinitio.aivoiceplatform.prompt.entity.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Prompt.
 *
 * <p>
 * Tenant relationship:
 *
 * <pre>
 * Prompt
 *   ↓
 * Agent
 *   ↓
 * Tenant
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface PromptRepository
        extends JpaRepository<Prompt, Long> {

    /**
     * Finds Prompt by public ID.
     *
     * @param publicId public ID
     * @return Prompt
     */
    Optional<Prompt> findByPublicId(
            String publicId
    );

    /**
     * Finds a non-deleted Prompt by public ID.
     *
     * @param publicId public ID
     * @param isDeleted deleted flag
     * @return Prompt
     */
    Optional<Prompt> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds all non-deleted Prompts globally.
     *
     * <p>
     * Used by SUPER_ADMIN.
     * </p>
     *
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated Prompts
     */
    Page<Prompt> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds all non-deleted Prompts belonging to a tenant.
     *
     * <p>
     * Tenant is resolved through:
     *
     * <pre>
     * Prompt → Agent → Tenant
     * </pre>
     *
     * @param tenantId tenant ID
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return paginated tenant Prompts
     */
    Page<Prompt> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Checks whether a Prompt code already exists.
     *
     * @param promptCode prompt code
     * @return true if code exists
     */
    boolean existsByPromptCode(
            String promptCode
    );

    /**
     * Checks whether a Prompt name already exists.
     *
     * @param promptName prompt name
     * @return true if name exists
     */
    boolean existsByPromptName(
            String promptName
    );

    List<Prompt> findAllByAgentIdAndIsDeleted(
            Long agentId,
            Integer isDeleted
    );
}
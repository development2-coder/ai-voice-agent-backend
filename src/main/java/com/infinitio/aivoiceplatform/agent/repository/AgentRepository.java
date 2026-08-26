package com.infinitio.aivoiceplatform.agent.repository;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository
        extends JpaRepository<Agent, Long> {

    // =========================================================
    // FIND
    // =========================================================

    Optional<Agent> findByPublicId(
            String publicId
    );

    Optional<Agent> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // CREATE VALIDATION
    // =========================================================

    boolean existsByAgentCodeAndTenantIdAndIsDeleted(
            String agentCode,
            Long tenantId,
            Integer isDeleted
    );

    boolean existsByAgentNameAndTenantIdAndIsDeleted(
            String agentName,
            Long tenantId,
            Integer isDeleted
    );


    // =========================================================
    // UPDATE VALIDATION
    // =========================================================

    boolean existsByAgentCodeAndTenantIdAndIsDeletedAndPublicIdNot(
            String agentCode,
            Long tenantId,
            Integer isDeleted,
            String publicId
    );

    boolean existsByAgentNameAndTenantIdAndIsDeletedAndPublicIdNot(
            String agentName,
            Long tenantId,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // PAGINATION
    // =========================================================

    Page<Agent> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );
}
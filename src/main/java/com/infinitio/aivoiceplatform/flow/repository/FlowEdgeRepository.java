package com.infinitio.aivoiceplatform.flow.repository;

import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Flow Edge Repository.
 *
 * Handles database operations for flow edges.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface FlowEdgeRepository
        extends JpaRepository<FlowEdge, Long> {

    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    Optional<FlowEdge> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND EDGES BY FLOW
    // =========================================================

    List<FlowEdge> findByFlowIdAndIsDeletedOrderByPriorityAsc(
            Long flowId,
            Integer isDeleted
    );


    // =========================================================
    // FIND EDGES BY SOURCE NODE
    // =========================================================

    List<FlowEdge> findBySourceNodeIdOrderByPriorityAsc(
            Long sourceNodeId
    );


    // =========================================================
    // FIND ACTIVE EDGES BY SOURCE NODE
    // =========================================================

    List<FlowEdge> findBySourceNodeIdAndIsDeletedOrderByPriorityAsc(
            Long sourceNodeId,
            Integer isDeleted
    );


    // =========================================================
    // CHECK DUPLICATE EDGE
    // =========================================================

    boolean existsBySourceNodeIdAndTargetNodeIdAndIsDeleted(
            Long sourceNodeId,
            Long targetNodeId,
            Integer isDeleted
    );


    // =========================================================
    // FIND EDGE BY SOURCE + TARGET
    // =========================================================

    Optional<FlowEdge> findBySourceNodeIdAndTargetNodeIdAndIsDeleted(
            Long sourceNodeId,
            Long targetNodeId,
            Integer isDeleted
    );


    // =========================================================
    // FIND EDGES BY TARGET NODE
    // =========================================================

    List<FlowEdge> findByTargetNodeIdAndIsDeleted(
            Long targetNodeId,
            Integer isDeleted
    );


    // =========================================================
    // FIND EDGES BY FLOW
    // =========================================================

    List<FlowEdge> findByFlowIdAndIsDeleted(
            Long flowId,
            Integer isDeleted
    );
}
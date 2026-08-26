package com.infinitio.aivoiceplatform.flow.repository;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Flow Node Repository.
 *
 * Handles database operations for flow nodes.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface FlowNodeRepository
        extends JpaRepository<FlowNode, Long> {

    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    Optional<FlowNode> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY FLOW
    // =========================================================

    List<FlowNode> findByFlowIdAndIsDeletedOrderByIdAsc(
            Long flowId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY FLOW + NODE TYPE
    // =========================================================

    Optional<FlowNode> findByFlowIdAndNodeTypeAndIsDeleted(
            Long flowId,
            FlowNodeType nodeType,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY FLOW + NODE KEY
    // =========================================================

    Optional<FlowNode> findByFlowIdAndNodeKeyAndIsDeleted(
            Long flowId,
            String nodeKey,
            Integer isDeleted
    );


    // =========================================================
    // CHECK DUPLICATE NODE KEY
    // =========================================================

    boolean existsByFlowIdAndNodeKeyAndIsDeleted(
            Long flowId,
            String nodeKey,
            Integer isDeleted
    );


    // =========================================================
    // CHECK DUPLICATE NODE KEY DURING UPDATE
    // =========================================================

    boolean existsByFlowIdAndNodeKeyAndIsDeletedAndPublicIdNot(
            Long flowId,
            String nodeKey,
            Integer isDeleted,
            String publicId
    );


    // =========================================================
    // COUNT ACTIVE NODES
    // =========================================================

    long countByFlowIdAndIsDeleted(
            Long flowId,
            Integer isDeleted
    );


    // =========================================================
    // FIND NODES BY FLOW
    // =========================================================

    List<FlowNode> findByFlowIdAndIsDeleted(
            Long flowId,
            Integer isDeleted
    );

    Optional<FlowNode> findByFlowIdAndNodeType(
            Long flowId,
            FlowNodeType nodeType
    );
}
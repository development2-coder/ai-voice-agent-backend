package com.infinitio.aivoiceplatform.flow.repository;

import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Flow edge persistence.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface FlowEdgeRepository
        extends JpaRepository<FlowEdge, Long> {

    /**
     * Finds an active edge by public ID.
     *
     * @param publicId edge public ID
     * @param isDeleted deletion flag
     * @return matching edge
     */
    Optional<FlowEdge> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds all active edges for a flow ordered by priority.
     *
     * @param flowId flow ID
     * @param isDeleted deletion flag
     * @return flow edges
     */
    List<FlowEdge> findByFlowIdAndIsDeletedOrderByPriorityAsc(
            Long flowId,
            Integer isDeleted
    );

    /**
     * Finds all edges originating from a node.
     *
     * @param sourceNodeId source node ID
     * @return edges
     */
    List<FlowEdge> findBySourceNodeIdOrderByPriorityAsc(
            Long sourceNodeId
    );

    /**
     * Finds active edges originating from a node.
     *
     * @param sourceNodeId source node ID
     * @param isDeleted deletion flag
     * @return edges
     */
    List<FlowEdge>
    findBySourceNodeIdAndIsDeletedOrderByPriorityAsc(
            Long sourceNodeId,
            Integer isDeleted
    );

    /**
     * Checks whether an active connection already exists between
     * the same source port and target port.
     *
     * @param sourceNodeId source node ID
     * @param sourcePort source output port
     * @param targetNodeId target node ID
     * @param targetPort target input port
     * @param isDeleted deletion flag
     * @return true if connection exists
     */
    boolean
    existsBySourceNodeIdAndSourcePortAndTargetNodeIdAndTargetPortAndIsDeleted(
            Long sourceNodeId,
            String sourcePort,
            Long targetNodeId,
            String targetPort,
            Integer isDeleted
    );

    /**
     * Finds an active edge using its complete connection identity.
     *
     * @param sourceNodeId source node ID
     * @param sourcePort source output port
     * @param targetNodeId target node ID
     * @param targetPort target input port
     * @param isDeleted deletion flag
     * @return matching edge
     */
    Optional<FlowEdge>
    findBySourceNodeIdAndSourcePortAndTargetNodeIdAndTargetPortAndIsDeleted(
            Long sourceNodeId,
            String sourcePort,
            Long targetNodeId,
            String targetPort,
            Integer isDeleted
    );

    /**
     * Finds active edges originating from a specific output port.
     *
     * @param sourceNodeId source node ID
     * @param sourcePort source port
     * @param isDeleted deletion flag
     * @return matching edges
     */
    List<FlowEdge>
    findBySourceNodeIdAndSourcePortAndIsDeletedOrderByPriorityAsc(
            Long sourceNodeId,
            String sourcePort,
            Integer isDeleted
    );

    /**
     * Finds active edges targeting a node.
     *
     * @param targetNodeId target node ID
     * @param isDeleted deletion flag
     * @return matching edges
     */
    List<FlowEdge> findByTargetNodeIdAndIsDeleted(
            Long targetNodeId,
            Integer isDeleted
    );

    /**
     * Finds active edges belonging to a flow.
     *
     * @param flowId flow ID
     * @param isDeleted deletion flag
     * @return matching edges
     */
    List<FlowEdge> findByFlowIdAndIsDeleted(
            Long flowId,
            Integer isDeleted
    );
}
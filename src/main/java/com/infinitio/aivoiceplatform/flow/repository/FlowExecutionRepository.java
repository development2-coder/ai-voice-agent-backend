package com.infinitio.aivoiceplatform.flow.repository;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for persistent flow execution operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowExecutionRepository
        extends JpaRepository<FlowExecution, Long> {

    /**
     * Finds a flow execution using its public identifier.
     *
     * @param publicId public identifier
     * @return matching flow execution
     */
    Optional<FlowExecution> findByPublicId(
            String publicId
    );

    /**
     * Finds the latest flow execution associated with a call.
     *
     * <p>
     * A call may have multiple flow executions during its
     * lifecycle, therefore the latest execution is required
     * for runtime state retrieval.
     * </p>
     *
     * @param callPublicId public identifier of the call
     * @return latest flow execution for the call
     */
    Optional<FlowExecution>
    findTopByCallPublicIdOrderByStartedAtDesc(
            String callPublicId
    );

    /**
     * Finds all flow executions associated with a call.
     *
     * @param callPublicId public identifier of the call
     * @return flow executions associated with the call
     */
    List<FlowExecution> findByCallPublicIdOrderByStartedAtAsc(
            String callPublicId
    );

    /**
     * Finds Flow executions waiting for a timer.
     *
     * @param status execution status
     * @return waiting executions
     */
    List<FlowExecution> findByStatus(
            FlowExecutionStatus status
    );
}
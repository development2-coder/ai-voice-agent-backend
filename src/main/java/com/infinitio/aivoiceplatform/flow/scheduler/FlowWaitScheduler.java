package com.infinitio.aivoiceplatform.flow.scheduler;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.repository.FlowExecutionRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionContinuationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Scheduler responsible for resuming Flow executions paused
 * by WAIT nodes.
 *
 * <p>
 * WAIT executions are persisted with a resume timestamp in
 * the Flow execution context. This scheduler periodically
 * checks expired executions and delegates their continuation
 * to the Flow execution continuation service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowWaitScheduler {

    /**
     * Scheduler polling interval.
     */
    private static final long FIXED_DELAY_MS = 5000L;

    /**
     * Maximum number of executions processed per cycle.
     */
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * Context key written by WaitNodeHandler.
     */
    private static final String WAIT_RESUME_AT =
            "_waitResumeAt";

    /**
     * Flow execution repository.
     */
    private final FlowExecutionRepository executionRepository;

    /**
     * Flow context service.
     *
     * <p>
     * The existing context service is reused instead of creating
     * a second ObjectMapper/serialization implementation.
     * </p>
     */
    private final FlowContextService flowContextService;

    /**
     * Flow continuation service.
     */
    private final FlowExecutionContinuationService continuationService;

    // =========================================================
    // SCHEDULER
    // =========================================================

    /**
     * Checks and resumes expired WAIT executions.
     */
    @Scheduled(
            fixedDelay = FIXED_DELAY_MS
    )
    public void processWaitingExecutions() {

        log.debug(
                "Starting Flow WAIT scheduler cycle."
        );

        List<FlowExecution> executions =
                executionRepository.findByStatus(
                        FlowExecutionStatus.WAITING_FOR_TIMER
                );

        if (executions == null
                || executions.isEmpty()) {

            log.debug(
                    "No Flow executions are waiting for timer."
            );

            return;
        }

        log.debug(
                "Flow WAIT scheduler found {} executions.",
                executions.size()
        );

        int processed = 0;

        for (FlowExecution execution : executions) {

            if (processed >= MAX_BATCH_SIZE) {

                log.warn(
                        "Flow WAIT scheduler batch limit reached. " +
                                "limit={}",
                        MAX_BATCH_SIZE
                );

                break;
            }

            if (execution == null) {
                continue;
            }

            try {

                if (!isExpired(
                        execution
                )) {
                    continue;
                }

                resumeExecution(
                        execution
                );

                processed++;

            } catch (Exception exception) {

                /*
                 * One failed execution must not stop processing
                 * other waiting executions.
                 */
                log.error(
                        "Failed to resume Flow WAIT execution. " +
                                "executionPublicId={}, error={}",
                        execution.getPublicId(),
                        exception.getMessage(),
                        exception
                );
            }
        }

        log.debug(
                "Completed Flow WAIT scheduler cycle. " +
                        "processed={}, totalFound={}",
                processed,
                executions.size()
        );
    }

    // =========================================================
    // EXPIRY
    // =========================================================

    /**
     * Determines whether a WAIT execution has expired.
     *
     * @param execution Flow execution
     * @return true when the WAIT duration has expired
     */
    private boolean isExpired(
            FlowExecution execution) {

        Map<String, Object> context =
                readContext(
                        execution
                );

        Object resumeAtValue =
                context.get(
                        WAIT_RESUME_AT
                );

        if (resumeAtValue == null) {

            log.error(
                    "WAIT resume timestamp is missing. " +
                            "executionPublicId={}",
                    execution.getPublicId()
            );

            return false;
        }

        LocalDateTime resumeAt =
                parseResumeTime(
                        execution,
                        resumeAtValue
                );

        if (resumeAt == null) {
            return false;
        }

        boolean expired =
                !LocalDateTime.now()
                        .isBefore(
                                resumeAt
                        );

        if (expired) {

            log.debug(
                    "Flow WAIT timer expired. " +
                            "executionPublicId={}, resumeAt={}",
                    execution.getPublicId(),
                    resumeAt
            );
        }

        return expired;
    }

    /**
     * Parses the persisted WAIT resume timestamp.
     *
     * @param execution Flow execution
     * @param value persisted timestamp
     * @return parsed timestamp or null when invalid
     */
    private LocalDateTime parseResumeTime(
            FlowExecution execution,
            Object value) {

        try {

            return LocalDateTime.parse(
                    String.valueOf(
                            value
                    )
            );

        } catch (Exception exception) {

            log.error(
                    "Invalid WAIT resume timestamp. " +
                            "executionPublicId={}, value={}",
                    execution.getPublicId(),
                    value,
                    exception
            );

            return null;
        }
    }

    // =========================================================
    // RESUME
    // =========================================================

    /**
     * Resumes an expired WAIT execution.
     *
     * @param execution expired execution
     */
    private void resumeExecution(
            FlowExecution execution) {

        log.info(
                "Resuming Flow execution after WAIT timer. " +
                        "executionPublicId={}",
                execution.getPublicId()
        );

        continuationService.continueAfterWait(
                execution.getPublicId()
        );

        log.info(
                "Flow WAIT execution resumed successfully. " +
                        "executionPublicId={}",
                execution.getPublicId()
        );
    }

    // =========================================================
    // CONTEXT
    // =========================================================

    /**
     * Reads execution context using the existing Flow context
     * service.
     *
     * @param execution Flow execution
     * @return execution context
     */
    private Map<String, Object> readContext(
            FlowExecution execution) {

        try {

            return flowContextService.readContext(
                    execution.getContextData()
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to read Flow execution context. " +
                            "executionPublicId={}",
                    execution.getPublicId(),
                    exception
            );

            return Map.of();
        }
    }
}
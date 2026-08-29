package com.infinitio.aivoiceplatform.flow.constant;

public enum FlowExecutionStatus {

    RUNNING,

    WAITING_FOR_INPUT,

    WAITING_FOR_AI,

    WAITING_FOR_API,

    TRANSFERRED,

    COMPLETED,

    FAILED,

    CANCELLED,

    WAITING_FOR_TIMER
}
package com.infinitio.aivoiceplatform.callsession.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a conversation message stored in a call session.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallConversationMessageDto implements Serializable {

    /**
     * Defines the role of the message sender.
     */
    private String role;

    /**
     * Contains the message text.
     */
    private String text;

    /**
     * Represents the time at which the message was created.
     */
    private Instant timestamp;
}
package com.infinitio.aivoiceplatform.callsession.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains configuration properties for call-session storage.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "voice.call-session")
public class CallSessionProperties {

    /**
     * Root directory used for local conversation storage.
     */
    private String conversationStoragePath =
            "./storage/conversations";
}
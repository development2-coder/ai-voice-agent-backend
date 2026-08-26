package com.infinitio.aivoiceplatform.tts.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configures access to generated TTS audio files.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TtsAudioResourceConfig
        implements WebMvcConfigurer {

    private final TtsProperties ttsProperties;

    /**
     * Registers the generated TTS audio resource location.
     *
     * @param registry Spring resource handler registry
     */
    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String storagePath =
                ttsProperties.getAudioStoragePath();

        if (storagePath == null
                || storagePath.isBlank()) {

            log.warn(
                    "TTS audio resource handler was not configured because audioStoragePath is missing."
            );

            return;
        }

        Path absolutePath =
                Paths.get(storagePath)
                        .toAbsolutePath()
                        .normalize();

        String resourceLocation =
                absolutePath
                        .toUri()
                        .toString();

        if (!resourceLocation.endsWith("/")) {

            resourceLocation += "/";
        }

        registry.addResourceHandler(
                "/tts-audio/**"
        ).addResourceLocations(
                resourceLocation
        );

        log.info(
                "TTS audio resource handler initialized. urlPattern=/tts-audio/**, storagePath={}",
                absolutePath
        );
    }
}
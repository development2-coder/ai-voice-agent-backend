package com.infinitio.aivoiceplatform.agentconfig.service.impl;

import com.infinitio.aivoiceplatform.agentconfig.config.AgentConfigurationOptionsProperties;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigurationOptionsResponse;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.ConfigurationOptionResponse;
import com.infinitio.aivoiceplatform.agentconfig.service.AgentConfigurationOptionsService;
import com.infinitio.aivoiceplatform.llm.config.LlmProperties;
import com.infinitio.aivoiceplatform.stt.config.SttProperties;
import com.infinitio.aivoiceplatform.tts.config.TtsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves selectable Agent Configuration options.
 *
 * <p>
 * Language options are derived from the configured STT, LLM and TTS
 * runtime capabilities. Speaker options are derived from the configured
 * TTS speakers. Greeting options are loaded from external application
 * configuration.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentConfigurationOptionsServiceImpl
        implements AgentConfigurationOptionsService {

    private final SttProperties sttProperties;

    private final LlmProperties llmProperties;

    private final TtsProperties ttsProperties;

    private final AgentConfigurationOptionsProperties
            optionsProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentConfigurationOptionsResponse getOptions() {

        log.info(
                "Fetching Agent Configuration options."
        );

        List<ConfigurationOptionResponse> languages =
                buildLanguageOptions();

        List<ConfigurationOptionResponse> speakers =
                buildSpeakerOptions();

        List<ConfigurationOptionResponse> greetings =
                buildGreetingOptions();

        log.info(
                "Agent Configuration options resolved successfully. " +
                        "languageCount={}, speakerCount={}, greetingCount={}",
                languages.size(),
                speakers.size(),
                greetings.size()
        );

        return AgentConfigurationOptionsResponse
                .builder()
                .languages(languages)
                .speakers(speakers)
                .greetings(greetings)
                .build();
    }

    /**
     * Builds language options from the configured runtime services.
     *
     * <p>
     * A language is exposed when it is supported by all configured
     * voice-runtime components that advertise supported languages.
     * If one runtime does not advertise any language list, it is treated
     * as unrestricted and does not remove languages from the result.
     * </p>
     *
     * @return language options
     */
    private List<ConfigurationOptionResponse>
    buildLanguageOptions() {

        List<String> sttLanguages =
                normalizeValues(
                        sttProperties.getSupportedLanguages()
                );

        List<String> llmLanguages =
                normalizeValues(
                        llmProperties.getSupportedLanguages()
                );

        List<String> ttsLanguages =
                normalizeValues(
                        ttsProperties.getSupportedLanguages()
                );

        Set<String> supportedLanguages =
                new LinkedHashSet<>();

        addCommonLanguages(
                supportedLanguages,
                sttLanguages,
                llmLanguages,
                ttsLanguages
        );

        return supportedLanguages
                .stream()
                .map(
                        language ->
                                buildOption(
                                        language,
                                        getLanguageLabel(language)
                                )
                )
                .toList();
    }

    /**
     * Builds speaker options from TTS runtime configuration.
     *
     * @return speaker options
     */
    private List<ConfigurationOptionResponse>
    buildSpeakerOptions() {

        Set<String> speakers =
                new LinkedHashSet<>();

        Map<String, List<String>> supportedSpeakers =
                ttsProperties.getSupportedSpeakers();

        if (supportedSpeakers == null
                || supportedSpeakers.isEmpty()) {

            log.warn(
                    "No TTS speakers are configured."
            );

            return List.of();
        }

        supportedSpeakers.values()
                .stream()
                .filter(
                        values ->
                                values != null
                )
                .flatMap(
                        Collection::stream
                )
                .filter(
                        value ->
                                value != null
                                        && !value.isBlank()
                )
                .map(
                        String::trim
                )
                .forEach(
                        speakers::add
                );

        return speakers
                .stream()
                .map(
                        speaker ->
                                buildOption(
                                        speaker,
                                        speaker
                                )
                )
                .toList();
    }

    /**
     * Builds greeting options from external application configuration.
     *
     * @return greeting options
     */
    private List<ConfigurationOptionResponse>
    buildGreetingOptions() {

        List<String> configuredGreetings =
                optionsProperties.getGreetings();

        if (configuredGreetings == null
                || configuredGreetings.isEmpty()) {

            log.warn(
                    "No Agent greeting options are configured."
            );

            return List.of();
        }

        return normalizeValues(
                configuredGreetings
        )
                .stream()
                .map(
                        greeting ->
                                buildOption(
                                        greeting,
                                        greeting
                                )
                )
                .toList();
    }

    /**
     * Adds languages supported by the configured runtime services.
     *
     * @param result target language set
     * @param sttLanguages STT supported languages
     * @param llmLanguages LLM supported languages
     * @param ttsLanguages TTS supported languages
     */
    private void addCommonLanguages(
            Set<String> result,
            List<String> sttLanguages,
            List<String> llmLanguages,
            List<String> ttsLanguages) {

        List<List<String>> configuredLanguageLists =
                new ArrayList<>();

        if (!sttLanguages.isEmpty()) {
            configuredLanguageLists.add(
                    sttLanguages
            );
        }

        if (!llmLanguages.isEmpty()) {
            configuredLanguageLists.add(
                    llmLanguages
            );
        }

        if (!ttsLanguages.isEmpty()) {
            configuredLanguageLists.add(
                    ttsLanguages
            );
        }

        if (configuredLanguageLists.isEmpty()) {

            log.warn(
                    "No runtime language options are configured."
            );

            return;
        }

        Set<String> commonLanguages =
                new LinkedHashSet<>(
                        configuredLanguageLists.get(0)
                );

        for (int index = 1;
             index < configuredLanguageLists.size();
             index++) {

            commonLanguages.retainAll(
                    configuredLanguageLists.get(index)
            );
        }

        result.addAll(
                commonLanguages
        );
    }

    /**
     * Normalizes configured string values.
     *
     * @param values configured values
     * @return normalized unique values
     */
    private List<String> normalizeValues(
            List<String> values) {

        if (values == null
                || values.isEmpty()) {

            return List.of();
        }

        return values
                .stream()
                .filter(
                        value ->
                                value != null
                                        && !value.isBlank()
                )
                .map(
                        String::trim
                )
                .distinct()
                .toList();
    }

    /**
     * Creates one configuration option response.
     *
     * @param value option value
     * @param label option display label
     * @return configuration option
     */
    private ConfigurationOptionResponse buildOption(
            String value,
            String label) {

        return ConfigurationOptionResponse
                .builder()
                .value(value)
                .label(label)
                .build();
    }

    /**
     * Returns the human-readable display label for a language code.
     *
     * <p>
     * The language code is kept as the option value because it is required
     * by the runtime services, while the label is displayed to the user.
     * </p>
     *
     * @param language language code
     * @return human-readable language label
     */
    private String getLanguageLabel(String language) {

        if (language == null || language.isBlank()) {
            return language;
        }

        return switch (language.trim().toLowerCase()) {
            case "en-in" -> "English";
            case "hi-in" -> "Hindi";
            case "mr-in" -> "Marathi";
            case "ta-in" -> "Tamil";
            case "te-in" -> "Telugu";
            case "bn-in" -> "Bengali";
            case "kn-in" -> "Kannada";
            case "ml-in" -> "Malayalam";
            case "gu-in" -> "Gujarati";
            case "pa-in" -> "Punjabi";
            default -> language;
        };
    }
}
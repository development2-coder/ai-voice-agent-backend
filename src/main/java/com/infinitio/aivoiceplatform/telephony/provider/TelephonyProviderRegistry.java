package com.infinitio.aivoiceplatform.telephony.provider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Registry for available telephony providers.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
public class TelephonyProviderRegistry {

    private final Map<String, TelephonyProvider> providers;

    /**
     * Creates the telephony provider registry.
     *
     * @param providerList available telephony providers
     */
    public TelephonyProviderRegistry(
            List<TelephonyProvider> providerList) {

        this.providers =
                providerList.stream()
                        .collect(
                                Collectors.toMap(
                                        provider ->
                                                provider
                                                        .getProviderCode()
                                                        .trim()
                                                        .toLowerCase(
                                                                java.util.Locale.ROOT
                                                        ),
                                        Function.identity()
                                )
                        );
    }

    /**
     * Gets a telephony provider by provider code.
     *
     * @param providerCode provider code
     * @return matching telephony provider
     */
    /**
     * Gets a telephony provider by provider code.
     *
     * @param providerCode provider code
     * @return matching telephony provider
     * @throws IllegalArgumentException when provider code is missing
     *                                  or unsupported
     */
    public TelephonyProvider getProvider(
            String providerCode) {

        if (providerCode == null
                || providerCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Telephony provider is required."
            );
        }

        String normalizedProviderCode =
                providerCode
                        .trim()
                        .toLowerCase(
                                java.util.Locale.ROOT
                        );

        TelephonyProvider provider =
                providers.get(
                        normalizedProviderCode
                );

        if (provider == null) {

            throw new IllegalArgumentException(
                    "Unsupported telephony provider: "
                            + providerCode
            );
        }

        return provider;
    }
}
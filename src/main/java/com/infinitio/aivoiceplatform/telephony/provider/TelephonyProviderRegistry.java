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
                                                        .toLowerCase(),
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
    public TelephonyProvider getProvider(
            String providerCode) {

        TelephonyProvider provider =
                providers.get(
                        providerCode.toLowerCase()
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
package dev.mfataka.transporter.model;

import java.util.Arrays;

/**
 * @author HAMMA FATAKA
 */
public enum TransporterClientResolver {
    NOOP_RESOLVER,
    DEFAULT_RESOLVER,
    CUSTOM,
    UNKNOWN;


    public static TransporterClientResolver fromName(final String resolver) {
        return Arrays.stream(TransporterClientResolver.values())
                .filter(resolvers -> resolvers.name().equalsIgnoreCase(resolver))
                .findFirst()
                .orElse(UNKNOWN);
    }
}

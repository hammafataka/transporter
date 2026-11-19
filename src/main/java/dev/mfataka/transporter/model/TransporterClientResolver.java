package dev.mfataka.transporter.model;

import java.util.Arrays;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 03.08.2022 13:49
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

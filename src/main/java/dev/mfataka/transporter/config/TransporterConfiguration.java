package dev.mfataka.transporter.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable value object carrying transporter settings.
 * Use {@link #toBuilder()} to derive a modified copy:
 * {@code config.toBuilder().baseUrl("...").build()}.
 *
 * @author HAMMA FATAKA
 */
@Value
@Builder(toBuilder = true)
public class TransporterConfiguration {
    boolean proxyEnabled;
    boolean followRedirection;
    boolean timeoutEnabled;
    boolean resolverEnabled;
    Boolean debugEnabled;
    Boolean mtlsEnabled;
    Boolean sslEnabled;
    Boolean loggerEnabled;
    Boolean logAuthHeaderEnabled;
    String resolver;
    String baseUrl;
    String trustStorePath;
    String trustStorePass;
    String keystorePath;
    String keystorePass;
    String proxyAddress;
    @Builder.Default
    Integer proxyPort = 0;
    @Builder.Default
    Long timeout = 0L;
    TimeUnit timeUnit;
    boolean checkRequiredFields;
    boolean requiredNonNull;
    Boolean trustAll;
    Integer dataLimit;
    String alias;
    String certPath;
    Integer maxConnections;
    Duration pendingAcquireTimeout;
    Integer pendingAcquireMaxCount;
    Boolean kibanaLoggingEnabled;
    String operator;

    public int getDataLimitOrDefault() {
        return dataLimit == null ? 256 * 1024 : dataLimit;
    }

    public Duration getPendingAcquireTimeoutOrDefault() {
        return pendingAcquireTimeout != null ? pendingAcquireTimeout : Duration.ofSeconds(10);
    }

    public int getPendingAcquireMaxCountOrDefault() {
        if (pendingAcquireMaxCount != null) return pendingAcquireMaxCount;
        return maxConnections != null ? 2 * maxConnections : 100;
    }

    public boolean isKibanaLoggingEnabled() {
        return Boolean.TRUE.equals(kibanaLoggingEnabled);
    }
}

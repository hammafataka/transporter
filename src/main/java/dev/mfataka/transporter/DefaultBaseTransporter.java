package dev.mfataka.transporter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import dev.mfataka.transporter.config.ConfigurationResolver;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.imp.TransporterBuilder;
import dev.mfataka.transporter.model.TransporterClientResolver;

/**
 * Stateless Spring-managed factory. Every {@code withX} returns a NEW
 * {@code DefaultBaseTransporter} carrying its own immutable
 * {@link TransporterConfiguration}; the receiver is never mutated.
 *
 * <p>The Spring-managed bean itself holds only the {@link ConfigurationResolver}
 * plus a default seed config — it is safe to share across threads.</p>
 *
 * @author HAMMA FATAKA
 */
@Slf4j
@Component
public class DefaultBaseTransporter implements BaseTransporter {
    private final ConfigurationResolver configurationResolver;
    private final TransporterConfiguration clientConfig;

    @Autowired
    public DefaultBaseTransporter(final ConfigurationResolver configurationResolver) {
        this(configurationResolver, defaultConfig());
    }

    private DefaultBaseTransporter(final ConfigurationResolver resolver,
                                   final TransporterConfiguration config) {
        this.configurationResolver = resolver;
        this.clientConfig = config;
    }

    private static TransporterConfiguration defaultConfig() {
        return TransporterConfiguration.builder()
                .timeout(60L)
                .timeUnit(TimeUnit.SECONDS)
                .build();
    }

    private BaseTransporter copy(final TransporterConfiguration newConfig) {
        return new DefaultBaseTransporter(configurationResolver, newConfig);
    }

    @Override
    public BaseTransporter withClientConfig(final TransporterConfiguration clientConfig) {
        return copy(clientConfig);
    }

    @Override
    public BaseTransporter withRedirection() {
        return copy(clientConfig.toBuilder().followRedirection(true).build());
    }

    @Override
    public BaseTransporter withResolver(TransporterClientResolver resolver) {
        return copy(clientConfig.toBuilder()
                .resolverEnabled(true)
                .resolver(resolver.name())
                .build());
    }

    @Override
    public BaseTransporter withTimeOut(long timeOut) {
        return withTimeOut(timeOut, TimeUnit.SECONDS);
    }

    @Override
    public BaseTransporter withTimeOut(long timeOut, @NotNull TimeUnit timeUnit) {
        return copy(clientConfig.toBuilder()
                .timeoutEnabled(true)
                .timeout(timeUnit.toSeconds(timeOut))
                .timeUnit(timeUnit)
                .build());
    }

    @Override
    public BaseTransporter secure(@NotNull final String trustStorePath, @NotNull final String trustStorePassword) {
        return copy(clientConfig.toBuilder()
                .sslEnabled(true)
                .trustStorePath(trustStorePath)
                .trustStorePass(trustStorePassword)
                .build());
    }

    @Override
    public BaseTransporter withMtls(@NotNull String keyStorePath, @NotNull String keyStorePassword) {
        return copy(clientConfig.toBuilder()
                .mtlsEnabled(true)
                .keystorePath(keyStorePath)
                .keystorePass(keyStorePassword)
                .build());
    }

    @Override
    public BaseTransporter withProxy(@NotNull final String proxyAddress, final int proxyPort, @NotNull final String proxySchema) {
        return copy(clientConfig.toBuilder()
                .proxyEnabled(true)
                .proxyAddress(proxyAddress)
                .proxyPort(proxyPort)
                .build());
    }

    @Override
    public BaseTransporter withDebugging(@NotNull Boolean enabled) {
        return copy(clientConfig.toBuilder().debugEnabled(enabled).build());
    }

    @Override
    public BaseTransporter withLogger() {
        return copy(clientConfig.toBuilder().loggerEnabled(true).build());
    }

    @Override
    public BaseTransporter trustAll() {
        return copy(clientConfig.toBuilder()
                .sslEnabled(false)
                .trustAll(true)
                .build());
    }

    @Override
    public BaseTransporter withConnectionPool(final int maxConnections, @NotNull final Duration pendingAcquireTimeout) {
        return copy(clientConfig.toBuilder()
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(pendingAcquireTimeout)
                .build());
    }

    @Override
    public BaseTransporter withConnectionPool(final int maxConnections, @NotNull final Duration pendingAcquireTimeout, final int pendingAcquireMaxCount) {
        return copy(clientConfig.toBuilder()
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(pendingAcquireTimeout)
                .pendingAcquireMaxCount(pendingAcquireMaxCount)
                .build());
    }

    @Override
    public TransporterBuilder toBuilder() {
        return TransporterBuilder.newBuilder(clientConfig, configurationResolver);
    }
}

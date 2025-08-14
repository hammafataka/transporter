package dev.mfataka.transporter;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mfataka.transporter.config.ConfigurationResolver;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.imp.DefaultTransporterBuilder;
import dev.mfataka.transporter.imp.TransporterBuilder;
import dev.mfataka.transporter.model.TransporterClientResolver;


/**
 * @author HAMMA FATAKA<br>
 * Project: base<br>
 * Date: 3/18/2022 1:04 PM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultBaseTransporter implements BaseTransporter {
    private TransporterConfiguration clientConfig = TransporterConfiguration.builder()
            .timeout(60L)
            .timeUnit(TimeUnit.SECONDS)
            .build();
    private final ConfigurationResolver configurationResolver;

    @Override
    public BaseTransporter withClientConfig(final TransporterConfiguration clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }


    @Override
    public BaseTransporter withRedirection() {
        this.clientConfig.setFollowRedirection(true);
        return this;
    }

    @Override
    public BaseTransporter withResolver(TransporterClientResolver resolver) {
        this.clientConfig.setResolverEnabled(true);
        this.clientConfig.setResolver(resolver.name());
        return this;
    }

    @Override
    public BaseTransporter withTimeOut(long timeOut) {
        return withTimeOut(timeOut, TimeUnit.SECONDS);
    }

    @Override
    public BaseTransporter withTimeOut(long timeOut, @NotNull TimeUnit timeUnit) {
        this.clientConfig.setTimeoutEnabled(true);
        this.clientConfig.setTimeout(timeUnit.toSeconds(timeOut));
        this.clientConfig.setTimeUnit(timeUnit);
        return this;
    }


    @Override
    public BaseTransporter secure(@NotNull final String trustStorePath, @NotNull final String trustStorePassword) {
        this.clientConfig.setSslEnabled(true);
        this.clientConfig.setTrustStorePath(trustStorePath);
        this.clientConfig.setTrustStorePass(trustStorePassword);
        return this;
    }

    @Override
    public BaseTransporter withMtls(@NotNull String keyStorePath, @NotNull String keyStorePassword) {
        this.clientConfig.setMtlsEnabled(true);
        this.clientConfig.setKeystorePath(keyStorePath);
        this.clientConfig.setKeystorePass(keyStorePassword);
        return this;
    }


    @Override
    public BaseTransporter withProxy(@NotNull final String proxyAddress, final int proxyPort, @NotNull final String proxySchema) {
        this.clientConfig.setProxyEnabled(true);
        this.clientConfig.setProxyAddress(proxyAddress);
        this.clientConfig.setProxyPort(proxyPort);
        return this;
    }

    @Override
    public BaseTransporter withDebugging(@NotNull Boolean enabled) {
        this.clientConfig.setDebugEnabled(enabled);
        return this;
    }

    @Override
    public BaseTransporter withLogger() {
        this.clientConfig.setLoggerEnabled(true);
        return this;
    }


    @Override
    public BaseTransporter trustAll() {
        this.clientConfig.setSslEnabled(false);
        this.clientConfig.setTrustAll(true);
        return this;
    }

    @Override
    public TransporterBuilder toBuilder() {
        configurationResolver.resolveBaseConfigs(clientConfig);
        return DefaultTransporterBuilder.of(clientConfig);
    }

}

package dev.mfataka.transporter.config;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.mfataka.transporter.model.TransporterClientResolver;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 22.08.2022 13:56
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigurationResolverImp implements ConfigurationResolver {
    private final Environment environment;

    @Override
    public TransporterClientResolver resolveResolver(@Nullable String resolver) {
        if (Objects.nonNull(resolver)) {
            return TransporterClientResolver.fromName(resolver);
        }
        final var maybeResolver = resolveProperty(RESOLVER_PROP);
        if (maybeResolver.isEmpty()) {
            return TransporterClientResolver.UNKNOWN;
        }

        return TransporterClientResolver.fromName(maybeResolver.get());
    }

    @Override
    public Optional<String> resolveTrustStorePath(@Nullable String path) {
        return resolveString(path, TRUST_STORE_PROP);
    }

    @Override
    public Optional<String> resolveTrustStorePassword(@Nullable String password) {
        return resolveString(password, TRUST_STORE_PASS_PROP);
    }

    @Override
    public Optional<String> resolveKeyStorePath(@Nullable String path) {
        return resolveString(path, KEY_STORE_PROP);
    }

    @Override
    public Optional<String> resolveKeyStorePassword(@Nullable String password) {
        return resolveString(password, KEY_STORE_PASS_PROP);
    }


    @Override
    public boolean resolveIsRequireNonNull(@Nullable final Boolean isRequireNonNull) {
        return resolveBool(isRequireNonNull, REQUIRED_NON_NULL_PROP);
    }

    @Override
    public boolean resolveIsCheckRequiredFields(@Nullable final Boolean checkRequiredFields) {
        return resolveBool(checkRequiredFields, CHECK_REQUIRED_FIELDS_PROP);
    }

    @Override
    public boolean resolveAuthHeaderLogging(@Nullable Boolean authHeaderLogging) {
        return resolveBool(authHeaderLogging, AUTH_HEADER_LOGGING_PROP);
    }

    @Override
    public boolean resolveLoggingEnabled(@Nullable Boolean loggingEnabled) {
        return resolveBool(loggingEnabled, LOGGING_ENABLED_PROP);
    }

    @Override
    public boolean resolveSecuredEnabled(@Nullable Boolean sslEnabled) {
        return resolveBool(sslEnabled, SECURED_ENABLED_PROP);
    }

    @Override
    public boolean resolveMtls(@Nullable Boolean mtlsEnabled) {
        return resolveBool(mtlsEnabled, MTLS_ENABLED_PROP);
    }

    @Override
    public boolean resolveDebugging(@Nullable final Boolean debugEnabled) {
        return resolveBool(debugEnabled, DEBUG_ENABLED_PROP);
    }

    @Override
    public boolean resolveTrustAll(@Nullable Boolean trustAll) {
        return resolveBool(trustAll, TRUST_ALL_PROP);
    }

    @Override
    public Optional<String> resolveAlias(@Nullable String alias) {
        return resolveString(alias, ALIAS_PROP);
    }

    @Override
    public boolean resolveKibanaLoggingEnabled(@Nullable Boolean kibanaLoggingEnabled) {
        return resolveBool(kibanaLoggingEnabled, KIBANA_LOGGING_ENABLED_PROP);
    }

    @Override
    public TransporterConfiguration resolveBaseConfigs(@NotNull TransporterConfiguration config) {
        final var resolver = resolveResolver(config.getResolver());
        final var sslEnabled = resolveSecuredEnabled(config.getSslEnabled());
        final var mtlsEnabled = resolveMtls(config.getMtlsEnabled());
        final var loggingEnabled = resolveLoggingEnabled(config.getLoggerEnabled());
        final var authHeaderLogging = resolveAuthHeaderLogging(config.getLogAuthHeaderEnabled());
        final var debugging = resolveDebugging(config.getDebugEnabled());
        final var kibanaLogging = resolveKibanaLoggingEnabled(config.getKibanaLoggingEnabled());

        final var builder = config.toBuilder()
                .resolver(resolver.name())
                .sslEnabled(sslEnabled)
                .mtlsEnabled(mtlsEnabled)
                .debugEnabled(debugging)
                .loggerEnabled(loggingEnabled)
                .logAuthHeaderEnabled(authHeaderLogging)
                .kibanaLoggingEnabled(kibanaLogging);

        resolveTrustStorePath(config.getTrustStorePath()).ifPresent(builder::trustStorePath);
        resolveTrustStorePassword(config.getTrustStorePass()).ifPresent(builder::trustStorePass);
        resolveKeyStorePath(config.getKeystorePath()).ifPresent(builder::keystorePath);
        resolveKeyStorePassword(config.getKeystorePass()).ifPresent(builder::keystorePass);
        resolveDataSize().ifPresent(builder::dataLimit);

        return builder.build();
    }

    private Optional<Integer> resolveDataSize() {
        final var maybeProperty = resolveProperty(ConfigurationResolverImp.DATA_LIMIT_PROP);
        if (maybeProperty.isEmpty()) {
            log.warn("{} is not in configs setting value to false, consider defining [{}]", ConfigurationResolverImp.DATA_LIMIT_PROP, ConfigurationResolverImp.DATA_LIMIT_PROP);
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(maybeProperty.get()));
    }

    private boolean resolveBool(@Nullable final Boolean boolValue, @NotNull final String propertyName) {
        if (Objects.nonNull(boolValue)) {
            return boolValue;
        }
        final var maybeProperty = resolveProperty(propertyName);
        if (maybeProperty.isEmpty()) {
            log.warn("{} is not in configs setting value to false, consider defining [{}]", propertyName, propertyName);
            return false;
        }
        return Boolean.parseBoolean(maybeProperty.get());
    }

    private Optional<String> resolveString(@Nullable final String maybeValue, @NotNull final String propertyName) {
        if (Objects.nonNull(maybeValue)) {
            return Optional.of(maybeValue);
        }
        final var maybeProperty = resolveProperty(propertyName);
        if (maybeProperty.isEmpty()) {
            log.error("{} is not in configs, consider defining [{}]", propertyName, propertyName);
            return Optional.empty();
        }
        return maybeProperty;
    }

    public Optional<String> resolveProperty(@NotNull final String property) {
        return Optional.ofNullable((environment.getProperty(property))
        );
    }
}

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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigurationResolverImp implements ConfigurationResolver {
    private static final String TRANSPORTER_PREFIX = "transporter";
    private static final String RESOLVER_PROP = TRANSPORTER_PREFIX + ".resolver";
    private static final String TRUST_STORE_PROP = TRANSPORTER_PREFIX + ".trustStorePath";
    private static final String TRUST_STORE_PASS_PROP = TRANSPORTER_PREFIX + ".trustStorePassword";
    private static final String KEY_STORE_PROP = TRANSPORTER_PREFIX + ".keyStorePath";
    private static final String KEY_STORE_PASS_PROP = TRANSPORTER_PREFIX + ".keyStorePassword";
    private static final String REQUIRED_NON_NULL_PROP = TRANSPORTER_PREFIX + ".requireNonNullEnabled";
    private static final String CHECK_REQUIRED_FIELDS_PROP = TRANSPORTER_PREFIX + ".checkRequiredFields";
    private static final String AUTH_HEADER_LOGGING_PROP = TRANSPORTER_PREFIX + ".authHeaderLoggingEnabled";
    private static final String LOGGING_ENABLED_PROP = TRANSPORTER_PREFIX + ".authHeaderLoggingEnabled";
    private static final String SECURED_ENABLED_PROP = TRANSPORTER_PREFIX + ".secured";
    private static final String MTLS_ENABLED_PROP = TRANSPORTER_PREFIX + ".mtlsEnabled";
    private static final String DEBUG_ENABLED_PROP = TRANSPORTER_PREFIX + ".debugEnabled";
    private static final String DATA_LIMIT_PROP = TRANSPORTER_PREFIX + ".dataLimit";
    private static final String TRUST_ALL_PROP = TRANSPORTER_PREFIX + ".trustAll";
    private static final String ALIAS_PROP = TRANSPORTER_PREFIX + ".alias";
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
    public void resolveBaseConfigs(@NotNull TransporterConfiguration config) {
        final var resolver = resolveResolver(config.getResolver());
        final var sslEnabled = resolveSecuredEnabled(config.getSslEnabled());
        final var mtlsEnabled = resolveMtls(config.getMtlsEnabled());
        final var loggingEnabled = resolveLoggingEnabled(config.getLoggerEnabled());
        final var authHeaderLogging = resolveAuthHeaderLogging(config.getLogAuthHeaderEnabled());
        final var debugging = resolveDebugging(config.getDebugEnabled());

        config.setResolver(resolver.name());
        config.setSslEnabled(sslEnabled);
        config.setMtlsEnabled(mtlsEnabled);
        config.setDebugEnabled(debugging);

        config.setLoggerEnabled(loggingEnabled);
        config.setLogAuthHeaderEnabled(authHeaderLogging);

        resolveTrustStorePath(config.getTrustStorePath())
                .ifPresent(config::setTrustStorePath);
        resolveTrustStorePassword(config.getTrustStorePass())
                .ifPresent(config::setTrustStorePass);

        resolveKeyStorePath(config.getKeystorePath())
                .ifPresent(config::setKeystorePath);

        resolveKeyStorePassword(config.getKeystorePass())
                .ifPresent(config::setKeystorePass);

        resolveDataSize()
                .ifPresent(config::setDataLimit);
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

    private Optional<String> resolveProperty(@NotNull final String property) {
        return Optional.ofNullable((environment.getProperty(property))
        );
    }
}

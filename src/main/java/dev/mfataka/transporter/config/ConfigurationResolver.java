package dev.mfataka.transporter.config;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import dev.mfataka.transporter.model.TransporterClientResolver;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 22.08.2022 13:52
 * purpose of this resolver is to get common configs for each transporter execution,
 * whenever transporter is used and the common configs are not set separately
 * it will try to resolve it by looking into configs
 */
public interface ConfigurationResolver {

    /**
     * If the given resolver is null, checks config files for 'transporter.resolver' if still not find then it returns unknown resolver.
     * otherwise returns valid value either passed or gets from property
     *
     * @param resolver The name of the resolver to use.
     * @return A resolver is being returned.
     */
    TransporterClientResolver resolveResolver(@Nullable final String resolver);

    /**
     * If the given path is null, checks config files for 'transporter.trustStorePath' if still not find then it returns failed DataResultable.
     * otherwise returns valid value either passed or gets from property
     *
     * @param path The path to the trust store.
     * @return A Optional<String>
     */
    Optional<String> resolveTrustStorePath(@Nullable final String path);

    /**
     * If the given password is null, checks config files for 'transporter.trustStorePassword' if still not find then it returns failed DataResultable.
     * otherwise returns valid value either passed or gets from property
     *
     * @param password The password to the trust store.
     * @return A Optional<String>
     */
    Optional<String> resolveTrustStorePassword(@Nullable final String password);

    /**
     * If the given path is null, checks config files for 'transporter.keyStorePath' if still not find then it returns failed DataResultable.
     * otherwise returns valid value either passed or gets from property
     *
     * @param path The path to the trust store.
     * @return A Optional<String>
     */
    Optional<String> resolveKeyStorePath(@Nullable final String path);

    /**
     * If the given password is null, checks config files for 'transporter.keyStorePassword' if still not find then it returns failed DataResultable.
     * otherwise returns valid value either passed or gets from property
     *
     * @param password The password to the trust store.
     * @return A Optional<String>
     */
    Optional<String> resolveKeyStorePassword(@Nullable final String password);

    /**
     * If the given isRequireNonNull is null, checks config files for 'transporter.requireNonNullEnabled' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @param isRequireNonNull The value of the annotation's isRequireNonNull parameter.
     * @return A boolean value.
     */
    boolean resolveIsRequireNonNull(@Nullable final Boolean isRequireNonNull);

    /**
     * If the given isRequireNonNull is null, checks config files for 'transporter.checkRequiredFields' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @param checkRequiredFields If true, the required fields will be checked.
     * @return A boolean value.
     */
    boolean resolveIsCheckRequiredFields(@Nullable final Boolean checkRequiredFields);

    /**
     * If the given authHeaderLogging is null, checks config files for 'transporter.authHeaderLoggingEnabled' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @param authHeaderLogging If true, the Authorization header will be logged.
     * @return A boolean value.
     */
    boolean resolveAuthHeaderLogging(@Nullable final Boolean authHeaderLogging);

    /**
     * If the given loggingEnabled is null, checks config files for 'transporter.loggingEnabled' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @param loggingEnabled The value of the loggingEnabled property in the configuration file.
     * @return A boolean value.
     */
    boolean resolveLoggingEnabled(@Nullable final Boolean loggingEnabled);

    /**
     * If the given sslEnabled is null, checks config files for 'transporter.sslEnabled' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @param sslEnabled The value of the sslEnabled property.
     * @return A boolean value.
     */
    boolean resolveSecuredEnabled(@Nullable final Boolean sslEnabled);

    /**
     * If the given mtlsEnabled is null, checks config files for 'transporter.mtlsEnabled' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @param mtlsEnabled The value of the mTLSEnabled property.
     * @return A boolean value.
     */
    boolean resolveMtls(@Nullable final Boolean mtlsEnabled);

    /**
     * it will set base configs used to build Transporter to TransporterConfiguration
     *
     * @param config The configuration object that will be used to resolve the base configurations.
     */
    void resolveBaseConfigs(@NotNull final TransporterConfiguration config);

    /**
     * If the given sslEnabled is null, checks config files for 'transporter.debugEnabled' if still not find then it returns false.
     * otherwise returns valid value either passed or gets from property
     *
     * @return A boolean value.
     */
    boolean resolveDebugging(@Nullable final Boolean debugEnabled);


    boolean resolveTrustAll(@Nullable final Boolean trustAll);

    Optional<String> resolveAlias(@Nullable final String alias);

}

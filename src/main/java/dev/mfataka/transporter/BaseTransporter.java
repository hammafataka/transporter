package dev.mfataka.transporter;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.model.TransporterClientResolver;
import dev.mfataka.transporter.imp.TransporterBuilder;

/**
 * @author HAMMA FATAKA<br>
 * Project: base<br>
 * Date: 3/18/2022 3:40 PM
 */
public interface BaseTransporter {


    /**
     * Sets the client configuration
     *
     * @param clientConfig The ClientConfiguration object to use when creating the client.
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withClientConfig(final TransporterConfiguration clientConfig);


    /**
     * configures client to follow redirection on 3xx status codes
     *
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withRedirection();

    /**
     * enabled client resolver for dns and hostname
     *
     * @param resolver The resolver to use for this transporter.
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withResolver(final TransporterClientResolver resolver);

    /**
     * Sets the timeout for the client
     *
     * @param timeOut The time-out for the request.
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withTimeOut(long timeOut);

    /**
     * Sets the timeout for the client.
     *
     * @param timeOut  The time-out value for the client.
     * @param timeUnit The time unit of the timeout.
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withTimeOut(long timeOut, @NotNull TimeUnit timeUnit);


    /**
     * will set configuration of ssl default protocol is TLS
     *
     * @param trustStorePath     a path to valid .jks file
     * @param trustStorePassword password if truststore file is secured with password
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter secure(@NotNull final String trustStorePath, @NotNull final String trustStorePassword);


    /**
     * enabled 2 way(mutual) tls.
     *
     * @param keyStorePath     The path to the keystore file.
     * @param keyStorePassword The password for the keystore file.
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withMtls(@NotNull final String keyStorePath, @NotNull final String keyStorePassword);

    /**
     * add proxy configurations for requested client
     *
     * @param proxyAddress address of proxy server
     * @param proxyPort    port of proxy server
     * @param proxySchema  schema of proxy if available
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withProxy(@NotNull final String proxyAddress, final int proxyPort, @NotNull final String proxySchema);

    BaseTransporter withDebugging(@NotNull final Boolean enabled);

    /**
     * adds {@link  org.zalando.logbook.Logbook} as logger
     *
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter withLogger();

    /**
     * will set configuration of ssl to trust all certificates
     *
     * @return {@link  BaseTransporter} for more config set
     */
    BaseTransporter trustAll();


    /**
     * will build preset configs to {@link  org.springframework.web.reactive.function.client.WebClient}
     *
     * @return {@link  TransporterBuilder} for more Transporter config setting
     */
    TransporterBuilder toBuilder();
}
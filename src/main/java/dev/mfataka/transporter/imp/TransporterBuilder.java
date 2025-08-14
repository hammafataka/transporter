package dev.mfataka.transporter.imp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.netty.resolver.dns.DnsAddressResolverGroup;

import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.model.TransporterClientResolver;

/**
 * @author HAMMA FATAKA<br>
 * Project: base<br>
 * Date: 3/21/2022 9:23 AM
 */
public interface TransporterBuilder {

    /**
     * method to create new instance of {@link TransporterBuilder}
     *
     * @param configuration configuration to be used
     * @return {@link TransporterBuilder}
     */
    static TransporterBuilder newBuilder(final TransporterConfiguration configuration) {
        return DefaultTransporterBuilder.of(configuration);
    }

    /**
     * method to set base url for webClient
     *
     * @param baseUrl base url to be set on client
     * @return {@link TransporterBuilder} for more configs
     */
    TransporterBuilder withBaseUrl(@NotNull final String baseUrl);


    /**
     * Sets the address resolver group, it is used for dns lookups, by default it is set to {@link reactor.netty.transport.NameResolverProvider.NameResolverSpec} to build it,
     * and it will produce to {@link DnsAddressResolverGroup}
     *
     * @param transporterClientResolver The resolver group to use.
     * @return A WebClientBuilder
     * @apiNote do not use it with complex configurations, do not set something else if it was not clear what you should use.
     */
    TransporterBuilder resolver(@NotNull final TransporterClientResolver transporterClientResolver);

    /**
     * method to apply rest services only
     *
     * @return {@link TransporterBuilder}
     * @see #soapService()
     */
    TransporterBuilder restService();

    /**
     * method to apply Soap services, this will make it to call, encode, decode Soap messages
     *
     * @return {@link  TransporterBuilder}
     * @see #restService()
     */
    TransporterBuilder soapService();


    TransporterBuilder dataLimit(final int limitInBytes);


    /**
     * It returns a WebClientMethodBuilder object.
     *
     * @return A WebClientMethodBuilder object.
     */
    Transporter build();

    /**
     * will execute POST request
     *
     * @param nullableBody can be null or set it will be sent as a body for request
     * @return {@link  DefaultTransporter} for more configs
     */
    Transporter post(@Nullable final Object nullableBody);

    /**
     * will execute GET request
     *
     * @param nullableBody can be null or set it will be sent as a body for request
     * @return {@link  DefaultTransporter} for more configs
     */
    Transporter get(@Nullable final Object nullableBody);

    /**
     * will execute PUT request
     *
     * @param nullableBody can be null or set it will be sent as a body for request
     * @return {@link  DefaultTransporter} for more configs
     */
    Transporter put(@Nullable final Object nullableBody);

    /**
     * will execute DELETE request
     *
     * @param nullableBody can be null or set it will be sent as a body for request
     * @return {@link  DefaultTransporter} for more configs
     */
    Transporter delete(@Nullable final Object nullableBody);

    /**
     * returns the web client with configurations applied
     *
     * @return {@link WebClient}
     */
    WebClient getWebClient();

    /**
     * method to get http service proxy factory
     *
     * @param name name of the factory
     * @return {@link HttpServiceProxyFactory}
     */

    HttpServiceProxyFactory getHttpServiceProxyFactory(final String name);

    /**
     * method to declare client, that will serve as client for the service of the given tClass
     *
     * @param tClass class of client
     * @param <T>    type of client, it will be used to create client, it is usually the interface of the client API
     * @return client
     */
    <T> T declareClient(final Class<T> tClass);

    /**
     * method to declare client, that will serve as client for the service of the given tClass
     *
     * @param tClass                  class of client
     * @param httpServiceProxyFactory factory to be used
     * @param <T>                     type of client, it will be used to create client, it is usually the interface of the client API
     * @return client
     */
    <T> T declareClient(final Class<T> tClass, final HttpServiceProxyFactory httpServiceProxyFactory);
}

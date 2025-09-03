package dev.mfataka.transporter.imp;

import java.io.File;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.netty.LogbookClientHandler;

import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsNameResolverBuilder;

import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import dev.mfataka.transporter.codec.JaxbDecoder;
import dev.mfataka.transporter.codec.JaxbEncoder;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.model.TransporterClientResolver;
import dev.mfataka.transporter.utils.LoggingUtils;
import dev.mfataka.transporter.utils.SslUtils;

/**
 * @author HAMMA FATAKA<br>
 * Project: base<br>
 * Date: 3/21/2022 9:24 AM
 */
@Slf4j
public class DefaultTransporterBuilder implements TransporterBuilder {
    private boolean isSoap;
    private Integer dataLimit;
    private final TransporterConfiguration config;
    private AddressResolverGroup<?> resolverGroup = NoopAddressResolverGroup.INSTANCE;
    private final LoggingUtils loggingUtils;
    private WebClient webClient;


    public static TransporterBuilder of(final TransporterConfiguration config) {
        return new DefaultTransporterBuilder(config, Boolean.TRUE.equals(config.getDebugEnabled()));
    }

    private DefaultTransporterBuilder(final TransporterConfiguration config, boolean isDebugEnabled) {
        this.config = config;
        this.loggingUtils = new LoggingUtils(log, isDebugEnabled);
    }


    @Override
    public TransporterBuilder withBaseUrl(@NotNull String baseUrl) {
        this.config.setBaseUrl(baseUrl);
        return this;
    }


    @Override
    public TransporterBuilder resolver(@NotNull TransporterClientResolver transporterClientResolver) {
        config.setResolverEnabled(true);
        config.setResolver(transporterClientResolver.name());
        return this;
    }

    @Override
    public TransporterBuilder restService() {
        this.isSoap = false;
        return this;
    }

    @Override
    public TransporterBuilder soapService() {
        loggingUtils.debugIfEnabled("soap services enabled.");
        this.isSoap = true;
        return this;
    }

    @Override
    public TransporterBuilder dataLimit(int limitInBytes) {
        this.dataLimit = limitInBytes;
        return this;
    }


    @Override
    public Transporter build() {
        return processBuildingClientMethod(null, null);
    }

    @Override
    public Transporter post(@Nullable final Object nullableBody) {
        return processBuildingClientMethod(TransporterMethod.POST, nullableBody);
    }

    @Override
    public Transporter get(@Nullable final Object nullableBody) {
        return processBuildingClientMethod(TransporterMethod.GET, nullableBody);
    }

    @Override
    public Transporter put(@Nullable Object nullableBody) {
        return processBuildingClientMethod(TransporterMethod.PUT, nullableBody);

    }

    @Override
    public Transporter delete(@Nullable Object nullableBody) {
        return processBuildingClientMethod(TransporterMethod.DELETE, nullableBody);
    }

    private Transporter processBuildingClientMethod(@Nullable final TransporterMethod method, @Nullable final Object body) {
        final var webClient = buildClient();
        return new DefaultTransporter(method, webClient, body, config);
    }

    private WebClient buildClient() {
        if (Objects.nonNull(webClient)) {
            return webClient;
        }
        final var builder = WebClient.builder();

        if (StringUtils.isNotEmpty(config.getBaseUrl())) {
            builder.baseUrl(config.getBaseUrl());
        }


        final var httpConnector = buildClientConnector();
        builder.clientConnector(httpConnector);
        loggingUtils.debugIfEnabled("success fully build reactive client with conf [{}]", config);

        if (isSoap) {
            builder.codecs(this::registerCodecs);
        }
        if (Objects.isNull(dataLimit) && Objects.nonNull(config.getDataLimit())) {
            configureDataLimit(builder, config.getDataLimit());
        }

        if (Objects.nonNull(dataLimit)) {
            configureDataLimit(builder, dataLimit);
        }

        final var client = builder.build();
        this.webClient = client;
        return client;
    }

    private void configureDataLimit(final WebClient.Builder builder, final Integer dataLimit) {
        loggingUtils.debugIfEnabled("data limit is set to [{}]", dataLimit);
        final ExchangeStrategies codecExchangeStrategy = ExchangeStrategies.builder()
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(dataLimit))
                .build();
        builder.exchangeStrategies(codecExchangeStrategy);
    }

    private void registerCodecs(final ClientCodecConfigurer configurer) {
        configurer.customCodecs().register(new JaxbEncoder());
        configurer.customCodecs().register(new JaxbDecoder());
        loggingUtils.debugIfEnabled("custom codecs added for soap services");
    }

    @NotNull
    private ReactorClientHttpConnector buildClientConnector() {
        var httpClient = HttpClient.create();

        if (Boolean.TRUE.equals(config.getSslEnabled()) || Boolean.TRUE.equals(config.getTrustAll())) {
            httpClient = buildSsl(httpClient);
            loggingUtils.debugIfEnabled("ssl is build");
        }

        if (config.isResolverEnabled()) {
            onResolverEnabled();
            httpClient = httpClient.resolver(resolverGroup);
        }


        if (config.isFollowRedirection()) {
            httpClient = httpClient.followRedirect(true);
            httpClient = httpClient.compress(true).followRedirect(true);
        }

        httpClient = buildProxy(httpClient);

        if (config.isTimeoutEnabled()) {
            httpClient = httpClient
                    .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(config.getTimeout(), config.getTimeUnit())));
            final var timeOut = (int) TimeUnit.SECONDS.toMillis(config.getTimeout());
            httpClient = httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut);
            httpClient = httpClient.responseTimeout(Duration.ofSeconds(config.getTimeout()));
            loggingUtils.debugIfEnabled("timeout is set to [{}] seconds for [responseTimeout,connectTimeout]", timeOut);
        }

        httpClient = buildLogger(httpClient);
        return new ReactorClientHttpConnector(httpClient);
    }

    private HttpClient buildProxy(@NotNull HttpClient httpClient) {
        if (config.isProxyEnabled()) {
            final var timeout = TimeUnit.SECONDS.toMillis(config.getTimeout() == null ? 60L : config.getTimeout());
            httpClient = httpClient.proxy(
                    proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                            .host(config.getProxyAddress())
                            .port(config.getProxyPort())
                            .connectTimeoutMillis(timeout)
                            .build()
            );
            loggingUtils.debugIfEnabled("proxy is set to address[{}], port[{}]", config.getProxyAddress(), config.getProxyPort());
        }
        return httpClient;
    }

    @NotNull
    private HttpClient buildSsl(@NotNull HttpClient httpClient) {
        try {
            final var timeOut = Duration.ofSeconds(Objects.isNull(config.getTimeout()) ? 60L : config.getTimeout());
            if (Boolean.TRUE.equals(config.getTrustAll()) && !config.getSslEnabled()) {
                final SslContext context = SslContextBuilder
                        .forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                httpClient = httpClient.secure(ssl -> ssl.sslContext(context)
                        .handshakeTimeout(timeOut)
                        .build()
                );
                loggingUtils.debugIfEnabled("ssl is build with trust all");

            } else if (Boolean.TRUE.equals(config.getSslEnabled())) {
                httpClient = httpClient.secure(ssl -> ssl
                        .sslContext(buildContext())
                        .handshakeTimeout(timeOut)
                        .build()
                );
            }
            loggingUtils.debugIfEnabled("ssl handshake timeout is [{}]", timeOut);
            return httpClient;
        } catch (Exception e) {
            log.error("failed to build ssl for transporter, message [{}]", e.getMessage(), e);
            throw new IllegalArgumentException("failed to build ssl for transporter", e);
        }
    }

    private SslContext buildContext() {
        final var defaultType = KeyStore.getDefaultType();
        final var algorithm = KeyManagerFactory.getDefaultAlgorithm();
        final String[] protocols = {"TLSv1.2", "TLSv1.3"};

        if (Objects.nonNull(config.getCertPath())) {
            final var sslContext = buildSslFromCert(protocols);
            loggingUtils.debugIfEnabled("ssl build successfully with cert path");
            return sslContext;
        }
        if (Boolean.TRUE.equals(config.getMtlsEnabled())) {
            if (Objects.nonNull(config.getAlias())) {
                final var sslContext = SslUtils.buildNettyContext(algorithm,
                        config.getTrustStorePath(),
                        config.getTrustStorePass(),
                        config.getTrustStorePath(),
                        config.getTrustStorePass(),
                        config.getAlias(),
                        defaultType,
                        protocols);
                loggingUtils.debugIfEnabled("mtls build successfully with alias");
                return sslContext;
            }
            final var sslContext = SslUtils.buildNettyContext(algorithm,
                    config.getTrustStorePath(),
                    config.getTrustStorePass(),
                    config.getKeystorePath(),
                    config.getKeystorePass(),
                    defaultType, protocols
            );
            loggingUtils.debugIfEnabled("mtls build successfully");
            return sslContext;
        }
        final var sslContext = SslUtils.buildNettyContextWithTrustStore(algorithm,
                config.getTrustStorePath(),
                config.getTrustStorePass(),
                defaultType,
                protocols
        );
        loggingUtils.debugIfEnabled("ssl build successfully");
        return sslContext;

    }

    @SneakyThrows
    private @NotNull SslContext buildSslFromCert(final String[] protocols) {
        return SslContextBuilder.forClient()
                .trustManager(new File(config.getCertPath()))
                .protocols(protocols)
                .build();
    }

    private HttpClient buildLogger(@NotNull HttpClient httpClient) {
        if (Boolean.TRUE.equals(config.getLoggerEnabled())) {
            final var loggerBuilder = Logbook
                    .builder()
                    .sink(new DefaultSink(
                                    new DefaultHttpLogFormatter(),
                                    new DefaultHttpLogWriter()
                            )
                    );
            if (Boolean.TRUE.equals(config.getLogAuthHeaderEnabled())) {
                loggerBuilder
                        .headerFilter(headers -> headers);
            }
            httpClient = httpClient.doOnConnected(connection ->
                    connection.addHandlerLast(new LogbookClientHandler(loggerBuilder.build()))
            );
        }
        return httpClient;
    }

    /**
     * Dangerous zone, please carefully :)
     */
    private void onResolverEnabled() {
        final var resolver = TransporterClientResolver.fromName(config.getResolver());
        switch (resolver) {
            case DEFAULT_RESOLVER:
                this.resolverGroup = DefaultAddressResolverGroup.INSTANCE;
                break;
            case CUSTOM:
                this.resolverGroup = new DnsAddressResolverGroup(
                        new DnsNameResolverBuilder()
                                .channelType(NioDatagramChannel.class)
                                .channelFactory(new ReflectiveChannelFactory<>(NioDatagramChannel.class))
                                .eventLoop(new DefaultEventLoop())
                                .socketChannelFactory(new ReflectiveChannelFactory<>(NioSocketChannel.class))

                );
                break;
            default:
                this.resolverGroup = NoopAddressResolverGroup.INSTANCE;
                break;
        }
    }

    @Override
    public WebClient getWebClient() {
        return buildClient();
    }

    @Override
    public HttpServiceProxyFactory getHttpServiceProxyFactory(final String name) {
        final var timeOut = Duration.of(config.getTimeout(), config.getTimeUnit().toChronoUnit());
        final var clientAdapter = WebClientAdapter.create(buildClient());
        clientAdapter.setBlockTimeout(timeOut);
        clientAdapter.setReactiveAdapterRegistry(ReactiveAdapterRegistry.getSharedInstance());
        return HttpServiceProxyFactory.builderFor(clientAdapter)
                .build();
    }

    @Override
    public <T> T declareClient(final Class<T> tClass) {
        return getHttpServiceProxyFactory(tClass.getSimpleName() + "_HttpServiceProxyFactory")
                .createClient(tClass);
    }

    @Override
    public <T> T declareClient(final Class<T> tClass, final HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(tClass);
    }
}

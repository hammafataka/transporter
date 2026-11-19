package dev.mfataka.transporter.imp;

import java.io.File;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
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
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultCorrelationId;
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
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;

import lombok.SneakyThrows;

import dev.mfataka.transporter.utils.SslUtils;

import dev.mfataka.transporter.codec.JaxbDecoder;
import dev.mfataka.transporter.codec.JaxbEncoder;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.filters.KibanaLoggingFilter;
import dev.mfataka.transporter.model.TransporterClientResolver;
import dev.mfataka.transporter.utils.Memoizer;
import dev.mfataka.transporter.utils.TransporterLog;

/**
 * Immutable builder. Each mutator returns a new instance carrying an
 * immutable {@link TransporterConfiguration}. The underlying {@link WebClient}
 * is built lazily and cached per-builder-instance via {@link Memoizer}.
 *
 * @author HAMMA FATAKA
 */
@Immutable
@ThreadSafe
public class DefaultTransporterBuilder implements TransporterBuilder {
    private static final TransporterLog log = new TransporterLog(DefaultTransporterBuilder.class);

    private final boolean isSoap;
    @Nullable
    private final TransporterClientResolver transporterClientResolver;
    private final TransporterConfiguration config;
    @Nullable
    private final WebClient explicitWebClient;
    private final Memoizer<WebClient> webClientCache;

    private DefaultTransporterBuilder(boolean isSoap,
                                      @Nullable TransporterClientResolver transporterClientResolver,
                                      TransporterConfiguration config,
                                      @Nullable WebClient explicitWebClient) {
        this.isSoap = isSoap;
        this.transporterClientResolver = transporterClientResolver;
        this.config = config;
        this.explicitWebClient = explicitWebClient;
        this.webClientCache = new Memoizer<>(() ->
                explicitWebClient != null ? explicitWebClient : buildClient());
    }

    public static TransporterBuilder of(final TransporterConfiguration config) {
        return new DefaultTransporterBuilder(false, null, config, null);
    }

    @Override
    public TransporterBuilder withBaseUrl(@NotNull String baseUrl) {
        return new DefaultTransporterBuilder(isSoap, transporterClientResolver,
                config.toBuilder().baseUrl(baseUrl).build(), explicitWebClient);
    }

    @Override
    public TransporterBuilder resolver(@NotNull TransporterClientResolver resolver) {
        return new DefaultTransporterBuilder(isSoap, resolver,
                config.toBuilder().resolver(resolver.name()).build(), explicitWebClient);
    }

    @Override
    public TransporterBuilder restService() {
        return new DefaultTransporterBuilder(false, transporterClientResolver, config, explicitWebClient);
    }

    @Override
    public TransporterBuilder soapService() {
        log.debugIfEnabled("soap services enabled.");
        return new DefaultTransporterBuilder(true, transporterClientResolver, config, explicitWebClient);
    }

    @Override
    public TransporterBuilder dataLimit(int limitInBytes) {
        return new DefaultTransporterBuilder(isSoap, transporterClientResolver,
                config.toBuilder().dataLimit(limitInBytes).build(), explicitWebClient);
    }

    @Override
    public TransporterBuilder operator(@NotNull String operator) {
        return new DefaultTransporterBuilder(isSoap, transporterClientResolver,
                config.toBuilder().operator(operator).build(), explicitWebClient);
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
        return DefaultTransporter.initial(webClientCache.get(), config, method, body);
    }

    private WebClient buildClient() {
        final var builder = WebClient.builder();
        if (StringUtils.isNotEmpty(config.getBaseUrl())) {
            builder.baseUrl(config.getBaseUrl());
        }
        builder.clientConnector(buildClientConnector());
        if (config.isKibanaLoggingEnabled()) {
            builder.filter(new KibanaLoggingFilter());
            log.debugIfEnabled("kibana call logging is enabled");
        }
        log.debugIfEnabled("successfully built reactive client with conf [{}]", config);
        builder.exchangeStrategies(configureExchangeStrategies());
        return builder.build();
    }

    private ExchangeStrategies configureExchangeStrategies() {
        final var builder = ExchangeStrategies.builder();
        if (isSoap) {
            builder.codecs(this::registerCodecs);
        }
        final var dataLimit = config.getDataLimitOrDefault();
        builder.codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(dataLimit));
        log.debugIfEnabled("data limit is set to [{}]", dataLimit);
        return builder.build();
    }

    private void registerCodecs(final ClientCodecConfigurer configurer) {
        configurer.customCodecs().register(new JaxbEncoder());
        configurer.customCodecs().register(new JaxbDecoder(config.getDataLimitOrDefault()));
        log.debugIfEnabled("custom codecs added for soap services");
    }

    @NotNull
    private ReactorClientHttpConnector buildClientConnector() {
        var httpClient = (config.getMaxConnections() != null)
                ? HttpClient.create(buildConnectionProvider())
                : HttpClient.create();
        if (Boolean.TRUE.equals(config.getSslEnabled()) || Boolean.TRUE.equals(config.getTrustAll())) {
            httpClient = buildSsl(httpClient);
            log.debugIfEnabled("ssl is built");
        }
        if (config.isResolverEnabled()) {
            httpClient = httpClient.resolver(onResolverEnabled());
        }
        if (config.isFollowRedirection()) {
            httpClient = httpClient.compress(true).followRedirect(true);
        }
        httpClient = buildProxy(httpClient);
        if (config.isTimeoutEnabled()) {
            httpClient = httpClient
                    .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(config.getTimeout(), config.getTimeUnit())));
            final var timeOut = (int) TimeUnit.SECONDS.toMillis(config.getTimeout());
            httpClient = httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut);
            httpClient = httpClient.responseTimeout(Duration.ofSeconds(config.getTimeout()));
            log.debugIfEnabled("timeout is set to [{}] seconds for [responseTimeout,connectTimeout]", timeOut);
        }
        httpClient = buildLogger(httpClient);
        return new ReactorClientHttpConnector(httpClient);
    }

    @NotNull
    private ConnectionProvider buildConnectionProvider() {
        final int max = config.getMaxConnections();
        final var provider = ConnectionProvider.builder("transporter")
                .maxConnections(max)
                .pendingAcquireTimeout(config.getPendingAcquireTimeoutOrDefault())
                .pendingAcquireMaxCount(config.getPendingAcquireMaxCountOrDefault())
                .build();
        log.debugIfEnabled("connection pool: maxConnections=[{}] pendingAcquireTimeout=[{}] pendingAcquireMaxCount=[{}]", max, config.getPendingAcquireTimeoutOrDefault(), config.getPendingAcquireMaxCountOrDefault());
        return provider;
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
            log.debugIfEnabled("proxy is set to address[{}], port[{}]", config.getProxyAddress(), config.getProxyPort());
        }
        return httpClient;
    }

    @NotNull
    private HttpClient buildSsl(@NotNull HttpClient httpClient) {
        try {
            final var timeOut = Duration.ofSeconds(Objects.isNull(config.getTimeout()) ? 60L : config.getTimeout());
            if (Boolean.TRUE.equals(config.getTrustAll()) && !Boolean.TRUE.equals(config.getSslEnabled())) {
                final SslContext context = SslContextBuilder
                        .forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = httpClient.secure(ssl -> ssl.sslContext(context).handshakeTimeout(timeOut).build());
                log.debugIfEnabled("ssl is build with trust all");
            } else if (Boolean.TRUE.equals(config.getSslEnabled())) {
                httpClient = httpClient.secure(ssl -> ssl.sslContext(buildContext()).handshakeTimeout(timeOut).build());
            }
            log.debugIfEnabled("ssl handshake timeout is [{}]", timeOut);
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
            log.debugIfEnabled("ssl build successfully with cert path");
            return sslContext;
        }
        if (Boolean.TRUE.equals(config.getMtlsEnabled())) {
            if (Objects.nonNull(config.getAlias())) {
                final var sslContext = SslUtils.buildNettyContext(algorithm,
                        config.getTrustStorePath(), config.getTrustStorePass(),
                        config.getTrustStorePath(), config.getTrustStorePass(),
                        config.getAlias(), defaultType, protocols);
                log.debugIfEnabled("mtls build successfully with alias");
                return sslContext;
            }
            final var sslContext = SslUtils.buildNettyContext(algorithm,
                    config.getTrustStorePath(), config.getTrustStorePass(),
                    config.getKeystorePath(), config.getKeystorePass(),
                    defaultType, protocols);
            log.debugIfEnabled("mtls build successfully");
            return sslContext;
        }
        final var sslContext = SslUtils.buildNettyContextWithTrustStore(algorithm,
                config.getTrustStorePath(), config.getTrustStorePass(), defaultType, protocols);
        log.debugIfEnabled("ssl build successfully");
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
                    .correlationId(sharedCorrelationId())
                    .sink(new DefaultSink(new DefaultHttpLogFormatter(), new DefaultHttpLogWriter()));
            if (Boolean.TRUE.equals(config.getLogAuthHeaderEnabled())) {
                loggerBuilder.headerFilter(headers -> headers);
            }
            httpClient = httpClient.doOnConnected(connection ->
                    connection.addHandlerLast(new LogbookClientHandler(loggerBuilder.build())));
        }
        return httpClient;
    }

    /**
     * Reuses the correlation id that {@link KibanaLoggingFilter} stamps on the
     * {@code X-Correlation-Id} header so that Logbook's request/response body
     * records carry the same id as the kibana marker record and can be paired in
     * kibana. Falls back to Logbook's default id when the header is absent (e.g.
     * kibana logging disabled).
     */
    private CorrelationId sharedCorrelationId() {
        final var fallback = new DefaultCorrelationId();
        return request -> {
            final var fromHeader = request.getHeaders().getFirst(KibanaLoggingFilter.CORRELATION_ID_HEADER);
            return fromHeader != null ? fromHeader : fallback.generate(request);
        };
    }

    /**
     * Dangerous zone, please carefully :)
     */
    private AddressResolverGroup<?> onResolverEnabled() {
        final var resolver = TransporterClientResolver.fromName(config.getResolver());
        return switch (resolver) {
            case DEFAULT_RESOLVER -> DefaultAddressResolverGroup.INSTANCE;
            case CUSTOM -> new DnsAddressResolverGroup(
                    new DnsNameResolverBuilder()
                            .datagramChannelType(NioDatagramChannel.class)
                            .datagramChannelFactory(new ReflectiveChannelFactory<>(NioDatagramChannel.class))
                            .eventLoop(new DefaultEventLoop())
                            .socketChannelFactory(new ReflectiveChannelFactory<>(NioSocketChannel.class)));
            default -> NoopAddressResolverGroup.INSTANCE;
        };
    }

    @Override
    public WebClient getWebClient() {
        return webClientCache.get();
    }

    @Override
    public HttpServiceProxyFactory getHttpServiceProxyFactory(final String name) {
        final var timeOut = Duration.of(config.getTimeout(), config.getTimeUnit().toChronoUnit());
        final var clientAdapter = WebClientAdapter.create(webClientCache.get());
        clientAdapter.setBlockTimeout(timeOut);
        clientAdapter.setReactiveAdapterRegistry(ReactiveAdapterRegistry.getSharedInstance());
        return HttpServiceProxyFactory.builderFor(clientAdapter).build();
    }

    @Override
    public <T> T declareClient(final Class<T> tClass) {
        return getHttpServiceProxyFactory(tClass.getSimpleName() + "_HttpServiceProxyFactory").createClient(tClass);
    }

    @Override
    public <T> T declareClient(final Class<T> tClass, final HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(tClass);
    }
}

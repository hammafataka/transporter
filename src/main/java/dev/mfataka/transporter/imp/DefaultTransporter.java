package dev.mfataka.transporter.imp;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;


import dev.mfataka.transporter.utils.JsonUtils;

import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.filters.KibanaLoggingFilter;
import dev.mfataka.transporter.utils.KibanaMarker;
import dev.mfataka.transporter.utils.TransporterLog;

/**
 * Immutable transporter. Every {@code withX} returns a new instance; the
 * receiver is never mutated. A single configured {@code DefaultTransporter}
 * is safe to share across threads and to reuse for many requests.
 *
 * @author HAMMA FATAKA
 */
@Immutable
@ThreadSafe
public final class DefaultTransporter implements Transporter {
    private static final TransporterLog log = new TransporterLog(DefaultTransporter.class);
    private static final String AUTHORIZATION = "Authorization";

    @Nullable
    private final TransporterMethod method;
    @Nullable
    private final Object body;
    @Nullable
    private final MediaType contentType;
    @Nullable
    private final String authHeader;
    private final WebClient webClient;
    private final TransporterConfiguration configuration;
    private final Map<String, String> headers;
    private final MultiValueMap<String, String> params;
    @Nullable
    private final String operator;

    private DefaultTransporter(@Nullable TransporterMethod method,
                               @Nullable Object body,
                               @Nullable MediaType contentType,
                               @Nullable String authHeader,
                               WebClient webClient,
                               TransporterConfiguration configuration,
                               Map<String, String> headers,
                               MultiValueMap<String, String> params,
                               @Nullable String operator) {
        this.method = method;
        this.body = body;
        this.contentType = contentType;
        this.authHeader = authHeader;
        this.webClient = webClient;
        this.configuration = configuration;
        this.headers = headers;
        this.params = params;
        this.operator = operator;
    }

    public static DefaultTransporter initial(final WebClient webClient,
                                             final TransporterConfiguration configuration,
                                             @Nullable final TransporterMethod method,
                                             @Nullable final Object body) {
        return new DefaultTransporter(method, body, null, null, webClient, configuration,
                Map.of(), CollectionUtils.unmodifiableMultiValueMap(new LinkedMultiValueMap<>()), null);
    }

    @Override
    public Transporter withHeader(@NotNull final String headerName, @NotNull final String headerValue) {
        log.debugIfEnabled("adding key[{}], value [{}] to headers", headerName, headerValue);
        if (headers.containsKey(headerName)) return this;
        var next = new LinkedHashMap<>(headers);
        next.put(headerName, headerValue);
        return new DefaultTransporter(method, body, contentType, authHeader, webClient, configuration,
                Map.copyOf(next), params, operator);
    }

    @Override
    public Transporter withUrlParam(@NotNull String paramName, @NotNull String paramValue) {
        log.debugIfEnabled("adding key[{}], value [{}] to url params", paramName, paramValue);
        var next = new LinkedMultiValueMap<>(params);
        next.add(paramName, paramValue);
        return new DefaultTransporter(method, body, contentType, authHeader, webClient, configuration,
                headers, CollectionUtils.unmodifiableMultiValueMap(next), operator);
    }

    @Override
    public Transporter withOperator(@NotNull final String operator) {
        log.debugIfEnabled("setting kibana operator [{}]", operator);
        return new DefaultTransporter(method, body, contentType, authHeader, webClient, configuration,
                headers, params, operator);
    }

    @Override
    public Transporter basicAuth(@NotNull String username, @NotNull String password) {
        final var auth = "Basic " + HttpHeaders.encodeBasicAuth(username, password, null);
        log.debugIfEnabled("basic auth is set");
        return new DefaultTransporter(method, body, contentType, auth, webClient, configuration, headers, params, operator);
    }

    @Override
    public Transporter bearerAuth(@NotNull String token) {
        final var auth = "Bearer " + token;
        log.debugIfEnabled("Bearer auth is set");
        return new DefaultTransporter(method, body, contentType, auth, webClient, configuration, headers, params, operator);
    }

    @Override
    public Transporter method(@NotNull TransporterMethod method) {
        return new DefaultTransporter(method, body, contentType, authHeader, webClient, configuration, headers, params, operator);
    }

    @Override
    public Transporter bodyValue(@NotNull Object body) {
        return new DefaultTransporter(method, body, contentType, authHeader, webClient, configuration, headers, params, operator);
    }

    @Override
    public Transporter contentType(@NotNull MediaType mediaType) {
        return new DefaultTransporter(method, body, mediaType, authHeader, webClient, configuration, headers, params, operator);
    }

    @Override
    public Transporter withContentLength() {
        Objects.requireNonNull(body);
        final var maybeSerializedBody = JsonUtils.parseObjectToJson(body);
        if (maybeSerializedBody.isFailure()) {
            log.error("Failed to set contentLength, message: could not serialize object to json");
            return this;
        }
        final var length = String.valueOf(maybeSerializedBody.get().getBytes(StandardCharsets.UTF_8).length);
        return withHeader(HttpHeaders.CONTENT_LENGTH, length);
    }

    @Override
    public WebClient.RequestBodySpec send(final @NotNull Function<UriBuilder, URI> uriFunction) {
        if (method == null) {
            throw new IllegalStateException("Cannot use send without specifying request method");
        }
        log.debugIfEnabled("sending [{}] to url function [{}]", method, uriFunction);
        final var spec = methodSpec().uri(uriFunction);
        return applyConfigured(spec);
    }

    @Override
    public WebClient.RequestBodySpec send(@NotNull final String url) {
        if (method == null) {
            throw new IllegalStateException("Cannot use send without specifying request method");
        }
        log.debugIfEnabled("sending [{}] to url [{}]", method, url);
        final var spec = methodSpec().uri(url);
        return applyConfigured(spec);
    }

    @Override
    public WebClient.RequestBodySpec send(@NotNull final String url, @NotNull final TransporterMethod method) {
        return this.method(method).send(url);
    }

    @Override
    public TransporterReceiver sendAndReceive(@NotNull Function<UriBuilder, URI> uriFunction) {
        return DefaultTransporterReceiver.of(configuration, send(uriFunction));
    }

    @Override
    public TransporterReceiver sendAndReceive(@NotNull final String url) {
        return DefaultTransporterReceiver.of(configuration, send(url));
    }

    @Override
    public TransporterReceiver sendAndReceive(@NotNull final String url, @NotNull final TransporterMethod method) {
        return this.method(method).sendAndReceive(url);
    }

    private WebClient.RequestBodyUriSpec methodSpec() {
        Objects.requireNonNull(method);
        return switch (method) {
            case POST -> webClient.post();
            case PUT -> webClient.put();
            case DELETE -> webClient.method(HttpMethod.DELETE);
            default -> webClient.method(HttpMethod.GET);
        };
    }

    @NotNull
    private WebClient.RequestBodySpec applyConfigured(final WebClient.RequestBodySpec spec) {
        if (body != null) spec.body(fromValue(body));
        if (authHeader != null) spec.header(AUTHORIZATION, authHeader);
        headers.forEach(spec::header);
        if (!params.isEmpty()) spec.body(fromFormData(params));
        if (contentType != null) spec.contentType(contentType);
        if (configuration.isKibanaLoggingEnabled()) {
            spec.attribute(KibanaLoggingFilter.MARKERS_ATTRIBUTE, resolveKibanaMarkers());
        }
        return spec;
    }

    /**
     * The resolved markers travel with the request itself and are read back by
     * {@link KibanaLoggingFilter} on whatever thread the exchange completes on.
     * The operator comes from the builder ({@code TransporterBuilder.operator(...)})
     * unless overridden per call via {@link #withOperator(String)}; the remaining
     * markers are derived or generated by the filter.
     */
    private Map<KibanaMarker, String> resolveKibanaMarkers() {
        final var markers = new EnumMap<KibanaMarker, String>(KibanaMarker.class);
        final var resolvedOperator = operator != null ? operator : configuration.getOperator();
        if (resolvedOperator != null) {
            markers.put(KibanaMarker.OPERATOR, resolvedOperator);
        }
        return Map.copyOf(markers);
    }
}

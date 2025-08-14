package dev.mfataka.transporter.imp;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import lombok.extern.slf4j.Slf4j;


import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.utils.JsonUtils;
import dev.mfataka.transporter.utils.LoggingUtils;

/**
 * @author HAMMA FATAKA<br>
 * Project: base<br>
 * Date: 3/19/2022 2:32 AM
 */
@Slf4j
public class DefaultTransporter implements Transporter {
    private TransporterMethod method;
    private Object body;
    private MediaType contentType;
    private String authHeader;
    private final WebClient webClient;
    private final TransporterConfiguration configuration;
    private final Map<String, String> headers = new HashMap<>();
    private final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    public final String AUTHORIZATION = "Authorization";
    private boolean authEnabled = false;
    private final LoggingUtils loggingUtils;

    public DefaultTransporter(final TransporterMethod method,
                              final WebClient webClient,
                              final Object body,
                              final TransporterConfiguration configuration) {
        this.method = method;
        this.webClient = webClient;
        this.body = body;
        this.configuration = configuration;
        this.loggingUtils = new LoggingUtils(log, Boolean.TRUE.equals(configuration.getDebugEnabled()));
    }

    @Override
    public Transporter withHeader(@NotNull final String headerName, @NotNull final String headerValue) {
        loggingUtils.debugIfEnabled("adding key[{}], value [{}] to headers", headerName, headerValue);
        headers.putIfAbsent(headerName, headerValue);
        return this;
    }

    @Override
    public Transporter withUrlParam(@NotNull String paramName, @NotNull String paramValue) {
        loggingUtils.debugIfEnabled("adding key[{}], value [{}] to url params", paramName, paramValue);
        params.add(paramName, paramValue);
        return this;
    }

    @Override
    public Transporter basicAuth(@NotNull String username, @NotNull String password) {
        this.authEnabled = true;
        this.authHeader = "Basic " + HttpHeaders.encodeBasicAuth(username, password, null);
        loggingUtils.debugIfEnabled("basic auth is set to [{}], readable format [{}],[{}]", authHeader, username, password);
        return this;
    }

    @Override
    public Transporter bearerAuth(@NotNull String token) {
        this.authEnabled = true;
        this.authHeader = "Bearer " + token;
        loggingUtils.debugIfEnabled("Bearer auth is set to [{}]", authHeader);
        return this;
    }

    @Override
    public Transporter method(@NotNull TransporterMethod method) {
        this.method = method;
        return this;
    }

    @Override
    public Transporter bodyValue(@NotNull Object body) {
        this.body = body;
        return this;
    }

    @Override
    public Transporter contentType(@NotNull MediaType mediaType) {
        this.contentType = mediaType;
        return this;
    }

    @Override
    public Transporter withContentLength() {
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
        loggingUtils.debugIfEnabled("sending [{}] to url function [{}]", method, uriFunction);

        final var requestBodySpec = (switch (method) {
            case POST -> webClient.post();
            case PUT -> webClient.put();
            case DELETE -> webClient.method(HttpMethod.DELETE);
            default -> webClient.method(HttpMethod.GET);
        }).uri(uriFunction);

        return buildBodyAndHeader(requestBodySpec);
    }

    @Override
    public WebClient.RequestBodySpec send(@NotNull final String url) {
        if (method == null) {
            throw new IllegalStateException("Cannot use send without specifying request method");
        }
        loggingUtils.debugIfEnabled("sending [{}] to url [{}]", method, url);

        final var requestBodySpec = (switch (method) {
            case POST -> webClient.post();
            case PUT -> webClient.put();
            case DELETE -> webClient.method(HttpMethod.DELETE);
            default -> webClient.method(HttpMethod.GET);
        }).uri(url);

        return buildBodyAndHeader(requestBodySpec);
    }

    public WebClient.RequestBodySpec send(@NotNull final String url, @NotNull final TransporterMethod method) {
        this.method = method;
        return send(uriBuilder -> uriBuilder.path(url).build());
    }

    @Override
    public TransporterReceiver sendAndReceive(@NotNull Function<UriBuilder, URI> uriFunction) {
        return DefaultTransporterReceiver.of(configuration, send(uriFunction));
    }

    @Override
    public TransporterReceiver sendAndReceive(@NotNull final String url) {
        return DefaultTransporterReceiver.of(configuration, send(url));
    }

    public TransporterReceiver sendAndReceive(@NotNull final String url, @NotNull final TransporterMethod method) {
        this.method = method;
        return sendAndReceive(url);
    }

    @NotNull
    private WebClient.RequestBodySpec buildBodyAndHeader(final WebClient.RequestBodySpec requestBodySpec) {
        try {
            if (body != null) {
                requestBodySpec.body(BodyInserters.fromValue(body));
            }
            if (authEnabled && Objects.nonNull(authHeader)) {
                headers.putIfAbsent(AUTHORIZATION, authHeader);
            }
            if (!headers.isEmpty()) {
                final var headerEntries = new ArrayList<>(headers.entrySet());
                headerEntries.forEach(header -> requestBodySpec.header(header.getKey(), header.getValue()));
            }
            if (!params.isEmpty()) {
                requestBodySpec.body(BodyInserters.fromFormData(params));
            }
            if (contentType != null) {
                requestBodySpec.contentType(contentType);
            }
            return requestBodySpec;
        } finally {
            body = null;
            authHeader = null;
            authEnabled = false;
            headers.clear();
            params.clear();
        }
    }
}

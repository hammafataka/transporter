package dev.mfataka.transporter.filters;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;

import reactor.core.publisher.Mono;

import dev.mfataka.transporter.utils.KibanaCallResult;
import dev.mfataka.transporter.utils.KibanaMarker;
import dev.mfataka.transporter.utils.TransporterLog;

/**
 * @author HAMMA FATAKA
 */
public final class KibanaLoggingFilter implements ExchangeFilterFunction {
    public static final String MARKERS_ATTRIBUTE = KibanaLoggingFilter.class.getName() + ".markers";
    /**
     * Header carrying the per-call correlation id on the wire. The id is
     * generated here at request time so that Logbook's request/response body
     * records (see {@code DefaultTransporterBuilder#buildLogger}) and this
     * filter's marker record share the same id and can be paired in kibana.
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final TransporterLog log = new TransporterLog(KibanaLoggingFilter.class);

    @Override
    @NotNull
    public Mono<ClientResponse> filter(@NotNull final ClientRequest request, @NotNull final ExchangeFunction next) {
        final var startNanos = System.nanoTime();
        final var correlationId = generateCorrelationId();
        final var tracedRequest = ClientRequest.from(request)
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();
        return next.exchange(tracedRequest)
                .doOnNext(response -> logResponse(tracedRequest, response, startNanos, correlationId))
                .doOnError(error -> logError(tracedRequest, error, startNanos, correlationId));
    }

    private void logResponse(final ClientRequest request, final ClientResponse response,
                             final long startNanos, final String correlationId) {
        final var status = response.statusCode();
        final var result = status.is2xxSuccessful() ? KibanaCallResult.SUCCESS : KibanaCallResult.FAIL;
        final var markers = resolveMarkers(request, result, startNanos, correlationId);
        markers.put(KibanaMarker.STATUS, String.valueOf(status.value()));
        log.kibanaCall(markers, "REST call [{}] finished with status [{}] result [{}]",
                markers.get(KibanaMarker.ENDPOINT), status.value(), result.value());
    }

    private void logError(final ClientRequest request, final Throwable error,
                          final long startNanos, final String correlationId) {
        final var result = isTimeout(error) ? KibanaCallResult.TIMEOUT : KibanaCallResult.FAIL;
        final var markers = resolveMarkers(request, result, startNanos, correlationId);
        log.kibanaCall(markers, "REST call [{}] failed with result [{}] message [{}]",
                markers.get(KibanaMarker.ENDPOINT), result.value(), error.getMessage());
    }

    private Map<KibanaMarker, String> resolveMarkers(final ClientRequest request,
                                                     final KibanaCallResult result,
                                                     final long startNanos,
                                                     final String correlationId) {
        final var markers = new EnumMap<KibanaMarker, String>(KibanaMarker.class);
        request.attribute(MARKERS_ATTRIBUTE)
                .filter(Map.class::isInstance)
                .map(this::asMarkerMap)
                .ifPresent(markers::putAll);
        markers.putIfAbsent(KibanaMarker.ENDPOINT, request.url().getPath());
        markers.put(KibanaMarker.CORRELATION_ID, correlationId);
        markers.put(KibanaMarker.RESULT, result.value());
        markers.put(KibanaMarker.DURATION, secondsSince(startNanos));
        return markers;
    }

    private String generateCorrelationId() {
        return String.format("%016x", ThreadLocalRandom.current().nextLong());
    }

    @SuppressWarnings("unchecked")
    private Map<KibanaMarker, String> asMarkerMap(final Object attribute) {
        return (Map<KibanaMarker, String>) attribute;
    }

    private String secondsSince(final long startNanos) {
        final var elapsedNanos = System.nanoTime() - startNanos;
        return String.format(Locale.ROOT, "%.3f", elapsedNanos / (double) TimeUnit.SECONDS.toNanos(1));
    }

    private boolean isTimeout(final Throwable error) {
        for (var cause = error; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            if (cause instanceof ReadTimeoutException
                    || cause instanceof TimeoutException
                    || cause instanceof ConnectTimeoutException) {
                return true;
            }
        }
        return false;
    }
}

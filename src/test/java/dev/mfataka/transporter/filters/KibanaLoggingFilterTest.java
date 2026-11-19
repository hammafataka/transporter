package dev.mfataka.transporter.filters;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import io.netty.handler.timeout.ReadTimeoutException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import dev.mfataka.transporter.utils.KibanaMarker;

class KibanaLoggingFilterTest {

    private final KibanaLoggingFilter filter = new KibanaLoggingFilter();
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        final var logger = (Logger) LoggerFactory.getLogger("kibana.KibanaLoggingFilter");
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        final var logger = (Logger) LoggerFactory.getLogger("kibana.KibanaLoggingFilter");
        logger.detachAppender(appender);
    }

    private static ClientRequest request(Map<KibanaMarker, String> markers) {
        final var builder = ClientRequest.create(HttpMethod.POST, URI.create("http://localhost/tap/register"));
        if (!markers.isEmpty()) {
            builder.attribute(KibanaLoggingFilter.MARKERS_ATTRIBUTE, markers);
        }
        return builder.build();
    }

    private Map<String, String> loggedFields() {
        assertThat(appender.list).hasSize(1);
        return appender.list.get(0).getMDCPropertyMap();
    }

    @Test
    void successResponse_logsSuccessWithStatusEndpointAndDuration() {
        final ExchangeFunction next = req -> Mono.just(ClientResponse.create(HttpStatus.OK).build());

        StepVerifier.create(filter.filter(request(Map.of(KibanaMarker.OPERATOR, "o2")), next))
                .expectNextCount(1)
                .verifyComplete();

        final var fields = loggedFields();
        assertThat(fields)
                .containsEntry("operator", "o2")
                .containsEntry("endpoint", "/tap/register")
                .containsEntry("http_status", "200")
                .containsEntry("result", "success")
                .containsKey("duration_s");
        assertThat(fields.get("correlation_id")).matches("[0-9a-f]{16}");
    }

    @Test
    void correlationIdIsAlwaysGenerated_callerValueIgnored() {
        final ExchangeFunction next = req -> Mono.just(ClientResponse.create(HttpStatus.OK).build());

        StepVerifier.create(filter.filter(request(Map.of(KibanaMarker.CORRELATION_ID, "caller-value")), next))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(loggedFields().get("correlation_id"))
                .isNotEqualTo("caller-value")
                .matches("[0-9a-f]{16}");
    }

    @Test
    void errorStatusResponse_logsFail() {
        final ExchangeFunction next = req -> Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());

        StepVerifier.create(filter.filter(request(Map.of()), next))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(loggedFields())
                .containsEntry("http_status", "500")
                .containsEntry("result", "fail");
    }

    @Test
    void timeoutError_logsTimeout() {
        final ExchangeFunction next = req -> Mono.error(ReadTimeoutException.INSTANCE);

        StepVerifier.create(filter.filter(request(Map.of()), next))
                .verifyError();

        assertThat(loggedFields())
                .containsEntry("result", "timeout")
                .containsEntry("endpoint", "/tap/register")
                .doesNotContainKey("http_status");
    }

    @Test
    void wrappedTimeoutError_logsTimeout() {
        final ExchangeFunction next = req -> Mono.error(new RuntimeException(new java.util.concurrent.TimeoutException("t")));

        StepVerifier.create(filter.filter(request(Map.of()), next))
                .verifyError();

        assertThat(loggedFields()).containsEntry("result", "timeout");
    }

    @Test
    void genericError_logsFail() {
        final ExchangeFunction next = req -> Mono.error(new IllegalStateException("boom"));

        StepVerifier.create(filter.filter(request(Map.of()), next))
                .verifyError();

        assertThat(loggedFields()).containsEntry("result", "fail");
    }

    @Test
    void explicitEndpointMarker_overridesUrlPath() {
        final ExchangeFunction next = req -> Mono.just(ClientResponse.create(HttpStatus.OK).build());

        StepVerifier.create(filter.filter(request(Map.of(KibanaMarker.ENDPOINT, "tap-register")), next))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(loggedFields()).containsEntry("endpoint", "tap-register");
    }

    @Test
    void mdcIsCleanedUpAfterEmit() {
        final ExchangeFunction next = req -> Mono.just(ClientResponse.create(HttpStatus.OK).build());

        StepVerifier.create(filter.filter(request(Map.of(KibanaMarker.OPERATOR, "o2")), next))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(org.slf4j.MDC.get("operator")).isNull();
        assertThat(org.slf4j.MDC.get("result")).isNull();
    }
}

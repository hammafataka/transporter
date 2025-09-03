package dev.mfataka.transporter.handlers;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

import dev.mfataka.transporter.model.TransporterData;

/**
 * @author HAMMA FATAKA
 */
@Slf4j
public class ErrorHandlers {

    /**
     * It handles errors thrown by the WebClient
     *
     * @param throwable The exception that was thrown.
     * @return A Mono
     */
    public static <T> Mono<TransporterData<T>> handleError(final Throwable throwable) {
        log.error("error sending request, message[{}]", throwable.getMessage(), throwable);
        if (throwable instanceof WebClientResponseException exception) {
            final var statusCode = exception.getStatusCode();
            return Mono.just(handleWithStatus(statusCode));
        } else if (throwable instanceof ResponseStatusException) {
            return Mono.just(resolveResponseStatusException(throwable));
        } else if (throwable instanceof UnsupportedMediaTypeException) {
            return Mono.just(handleEntity(new ResponseEntity<>(null, HttpStatus.UNSUPPORTED_MEDIA_TYPE)));
        } else {
            log.error("Could not identify error rethrowing it..");
            return Mono.just(TransporterData.fail("could not identify throwable", throwable));
        }
    }

    /**
     * It takes a `ResponseStatusException` and returns a Mono with the same status code as the
     * exception
     *
     * @param throwable The exception that was thrown.
     * @return A Mono
     */
    public static <T> TransporterData<T> resolveResponseStatusException(final Throwable throwable) {
        final var exception = (ResponseStatusException) throwable;
        final var statusCode = exception.getStatusCode();
        return handleWithStatus(statusCode);
    }


    /**
     * If the status code is one of the following, return a response entity with the appropriate status code:
     * <p>
     * - 400
     * - 401
     * - 403
     * - 404
     * - 405
     * - 406
     * - 409
     * - 415
     * - 422
     * - 429
     * - 500
     * - 501
     * - 503
     * - 504
     * <p>
     * Otherwise, if the status code is in the 400 series, return a 400 response entity
     *
     * @param statusCode The HTTP status code of the response.
     * @return A Mono
     */
    @NotNull
    public static <T> TransporterData<T> handleWithStatus(final HttpStatusCode statusCode) {
        final TransporterData<T> maybeResolved = handleEntity(new ResponseEntity<>(null, statusCode));
        if (!maybeResolved.isUnknown()) {
            return maybeResolved;
        }
        if (statusCode.is4xxClientError()) {
            return TransporterData.fail("Received Client error, the error is on client(our) side probably");
        }
        if (statusCode.is3xxRedirection()) {
            return TransporterData.fail("Received redirection on target server side");
        }

        if (statusCode.is5xxServerError()) {
            return TransporterData.fail("Received server error on target server side");
        }
        if (statusCode.is1xxInformational()){
            return TransporterData.fail("Received server informational on target server side");
        }
        return TransporterData.fail("Received non 2xx status code on target server side");
    }

    @NotNull
    public static <T> TransporterData<T> handleErrorResponse(final ResponseEntity<T> entity) {
        final var statusCode = entity.getStatusCode();
        final var maybeResolved = handleEntity(Objects.requireNonNull(entity));
        if (!maybeResolved.isUnknown()) {
            return maybeResolved;
        }
        if (statusCode.is4xxClientError()) {
            return TransporterData.fail("Received Client error, the error is on client side");
        }
        if (statusCode.is3xxRedirection()) {
            return TransporterData.fail("Received redirection on client side");
        }

        return TransporterData.fail("Received internal server error from client");
    }

    /**
     * It takes a `HttpStatus` as an argument and returns a Mono if the status is one of the
     * following: `NOT_FOUND`, `TEMPORARY_REDIRECT`, `PERMANENT_REDIRECT`, `INTERNAL_SERVER_ERROR`, `FORBIDDEN`,
     * `UNAUTHORIZED`, `UNSUPPORTED_MEDIA_TYPE`, `BAD_REQUEST`
     *
     * @param response The HTTP response.
     */
    @NotNull
    public static <T> TransporterData<T> handleEntity(final ResponseEntity<T> response) {
        final var statusCode = response.getStatusCode();
        final var httpStatus = HttpStatus.valueOf(statusCode.value());
        switch (httpStatus) {
            case NOT_FOUND -> {
                log.error("provided uri is not found");
                return TransporterData.failWithStatus("Received 404 not found, probably sent wrong url to target server", httpStatus);
            }
            case TEMPORARY_REDIRECT, PERMANENT_REDIRECT -> {
                log.warn("Received redirection, target server uses different url");
                return TransporterData.failWithStatus("Received redirection from client", httpStatus);
            }
            case INTERNAL_SERVER_ERROR -> {
                log.warn("target server responded with 500");
                return TransporterData.failWithStatus("Received internal server error from target server", httpStatus);
            }
            case FORBIDDEN -> {
                log.error("Received 403 (forbidden) from target server");
                return TransporterData.failWithStatus("Received 403 forbidden from target server, wrong credentials we have sent :)", httpStatus);
            }
            case UNAUTHORIZED -> {
                log.error("Received 401 (Unauthorized) from target server");
                return TransporterData.failWithStatus("Received 401 (Unauthorized) from target server, probably we need to provide some authentication", httpStatus);
            }
            case UNSUPPORTED_MEDIA_TYPE -> {
                log.error("Received 415 Wrong method call from target server");
                return TransporterData.failWithStatus("Received 415 unsupported media type  from target server. probably it is better to specify content type correctly", httpStatus);
            }
            case BAD_REQUEST -> {
                log.error("Received 400 unsatisfied request from target server");
                final T body = response.getBody();
                if (body != null) {
                    log.debug("Response: [{}]", response.getBody());
                    return TransporterData.failWithStatus("Received 400 bad request from target server", body, httpStatus);
                }
                return TransporterData.failWithStatus("Received 400 bad request from target server", httpStatus);
            }
        }
        return TransporterData.unknown("could not identify error status", httpStatus);
    }
}

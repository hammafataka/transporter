package dev.mfataka.transporter.checkers;

import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.vavr.control.Try;

import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

import dev.mfataka.transporter.handlers.ErrorHandlers;
import dev.mfataka.transporter.utils.JsonUtils;
import dev.mfataka.transporter.validators.NotNullValidator;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 07.09.2022 13:53
 */
@Slf4j
public final class EntityCheckers {

    public static <T> Mono<ResponseEntity<T>> resolveEntityMapping(final ClientResponse response,
                                                                   final Class<T> responseType,
                                                                   final boolean requireNonNull,
                                                                   final boolean checkFields) {

        if (checkFields) {
            if (response.statusCode().isError()) {
                return response.toEntity(responseType)
                        .map(tResponseEntity -> ErrorHandlers.handleErrorResponse(tResponseEntity).asResponseEntity());
            }
            return response.bodyToMono(String.class)
                    .<T>handle((data, sink) -> {
                        final var maybeResult = JsonUtils.parseJsonToObject(data, responseType);
                        if (maybeResult.isFailure()) {
                            final var cause = maybeResult.getCause();
                            log.error("could not extract response, message [{}]", cause.getMessage(), cause);
                            sink.error(cause);
                            return;
                        }
                        sink.next(maybeResult.get());
                    })
                    .map(ResponseEntity::ok);
        }
        return checkForEntityMapping(response, requireNonNull, responseType);
    }

    public static <T> Mono<ResponseEntity<T>> checkForEntityMapping(final ClientResponse response,
                                                                    final boolean requireNonNull,
                                                                    final Class<T> responseType) {

        if (response.statusCode().isError()) {
            return response.toEntity(responseType)
                    .map(tResponseEntity -> ErrorHandlers.handleErrorResponse(tResponseEntity).asResponseEntity());
        }
        return response
                .bodyToMono(responseType)
                .flatMap(mapper -> {
                    final var body = Objects.requireNonNull(mapper);
                    final var validator = new NotNullValidator(body.getClass());
                    if (requireNonNull) {
                        return validator.handleRequireNonNull(mapper)
                                .map(ResponseEntity::ok);
                    }
                    return validator.processMonoBody(mapper)
                            .map(ResponseEntity::ok);
                });
    }
}

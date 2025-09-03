package dev.mfataka.transporter.checkers;

import java.util.Objects;

import org.springframework.web.reactive.function.client.ClientResponse;

import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;


import dev.mfataka.transporter.handlers.ErrorHandlers;
import dev.mfataka.transporter.model.TransporterData;
import dev.mfataka.transporter.utils.JsonUtils;
import dev.mfataka.transporter.validators.NotNullValidator;

/**
 * @author HAMMA FATAKA
 * purpose of this class is to check deeply in client responses and resolve them in case of not processable data or successfully responses,
 * it handles errors if it occurred
 */
@Slf4j
public final class MappingChecker {
    public static <T> Mono<TransporterData<T>> resolveMapping(final ClientResponse response,
                                                              final Class<T> responseType,
                                                              final boolean requireNonNull,
                                                              final boolean checkFields) {
        if (checkFields) {
            if (response.statusCode().isError()) {
                return response.toEntity(responseType)
                        .map(ErrorHandlers::handleErrorResponse);
            }
            return response
                    .bodyToMono(String.class)
                    .handle((data, sink) -> {
                        final var maybeResult = JsonUtils.parseJsonToObject(data, responseType);
                        if (maybeResult.isFailure()) {
                            final var cause = maybeResult.getCause();
                            log.error("could not extract response, message [{}]", cause.getMessage());
                            sink.error(cause);
                            return;
                        }
                        sink.next(TransporterData.fromTry(maybeResult));
                    });
        }
        return checkForMapping(response, responseType, requireNonNull);
    }

    public static <T> Mono<TransporterData<T>> checkForMapping(final ClientResponse response,
                                                               final Class<T> responseType,
                                                               final boolean requireNonNull) {
        if (response.statusCode().isError()) {
            return response.toEntity(responseType)
                    .map(ErrorHandlers::handleErrorResponse);
        }
        return response
                .toEntity(responseType)
                .flatMap(mapper -> {
                    if (requireNonNull) {
                        if (Objects.isNull(mapper.getBody())) {
                            log.error("body was empty, cannot check for fields...");
                            return Mono.just(TransporterData.fail("body was empty"));
                        }
                        final var body = mapper.getBody();
                        final var validator = new NotNullValidator(body.getClass());
                        return validator.handleRequest(mapper);
                    }
                    return Mono.just(TransporterData.fromResponseEntity(mapper));
                });


    }
}

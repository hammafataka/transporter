package dev.mfataka.transporter.checkers;

import java.util.Objects;

import org.springframework.web.reactive.function.client.ClientResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;


import dev.mfataka.transporter.handlers.ErrorHandlers;
import dev.mfataka.transporter.utils.JsonUtils;
import dev.mfataka.transporter.validators.NotNullValidator;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 14.05.2023 3:44
 */
@Slf4j
public final class FluxCheckers {

    public static <T> Flux<T> resolveMapping(final ClientResponse response,
                                             final Class<T> responseType,
                                             final boolean requireNonNull,
                                             final boolean checkFields) {
        if (checkFields) {
            if (response.statusCode().isError()) {
                return response.toEntity(responseType)
                        .flatMapMany(tResponseEntity -> {
                            final var tTransporterData = ErrorHandlers.handleErrorResponse(tResponseEntity);
                            return Flux.error(new Throwable(tTransporterData.resultMessage()));
                        });
            }
            return response.bodyToFlux(String.class)
                    .<T>handle((data, sink) -> {
                        final var maybeResult = JsonUtils.parseJsonToObject(data, responseType);
                        if (maybeResult.isFailure()) {
                            final var cause = maybeResult.getCause();
                            log.error("could not extract response, message [{}]", cause.getMessage());
                            sink.error(cause);
                            return;
                        }
                        sink.next(maybeResult.get());
                    })
                    .onErrorResume(Mono::error);
        }

        return checkForFluxMapping(response, requireNonNull, responseType);
    }


    public static <T> Flux<T> checkForFluxMapping(final ClientResponse response,
                                                  final boolean requireNonNull,
                                                  final Class<T> responseType) {
        if (response.statusCode().isError()) {
            return response.toEntity(responseType)
                    .flatMapMany(tResponseEntity -> {
                        final var tTransporterData = ErrorHandlers.handleErrorResponse(tResponseEntity);
                        return Flux.error(new Throwable(tTransporterData.resultMessage()));
                    });
        }
        return response
                .bodyToFlux(responseType)
                .flatMap(mapper -> {
                    final var body = Objects.requireNonNull(mapper);
                    final var validator = new NotNullValidator(body.getClass());
                    if (requireNonNull) {
                        return validator.handleRequireNonNull(mapper);
                    }
                    return validator.processMonoBody(mapper);
                });
    }

}

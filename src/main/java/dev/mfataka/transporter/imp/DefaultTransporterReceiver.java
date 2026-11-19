package dev.mfataka.transporter.imp;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import dev.mfataka.transporter.checkers.EntityCheckers;
import dev.mfataka.transporter.checkers.FluxCheckers;
import dev.mfataka.transporter.checkers.MappingChecker;
import dev.mfataka.transporter.checkers.MonoCheckers;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.handlers.ErrorHandlers;
import dev.mfataka.transporter.model.TransporterData;

/**
 * Immutable receiver. {@link #requireNonNull(Boolean)} and
 * {@link #checkRequiredFields(Boolean)} return new receiver instances; they
 * never write back to the shared {@link TransporterConfiguration}.
 *
 * @author HAMMA FATAKA
 */
@Immutable
@ThreadSafe
public final class DefaultTransporterReceiver implements TransporterReceiver {
    private final TransporterConfiguration configuration;
    private final WebClient.RequestBodySpec requestBodySpec;
    private final boolean requireNonNull;
    private final boolean checkRequiredFields;

    private DefaultTransporterReceiver(TransporterConfiguration configuration,
                                       WebClient.RequestBodySpec spec,
                                       boolean requireNonNull,
                                       boolean checkRequiredFields) {
        this.configuration = configuration;
        this.requestBodySpec = spec;
        this.requireNonNull = requireNonNull;
        this.checkRequiredFields = checkRequiredFields;
    }

    public static DefaultTransporterReceiver of(TransporterConfiguration cfg,
                                                WebClient.RequestBodySpec spec) {
        return new DefaultTransporterReceiver(cfg, spec,
                cfg.isRequiredNonNull(), cfg.isCheckRequiredFields());
    }

    @Override
    public <T> Mono<TransporterData<T>> transporterData(@NotNull final Class<T> responseType) {
        return requestBodySpec
                .exchangeToMono(mapper -> getTransporterDataMono(responseType, mapper))
                .onErrorResume(ErrorHandlers::handleError)
                .onErrorReturn(TransporterData.fail("Failed to return data"));
    }

    @Override
    public <T> TransporterData<T> transporterDataBlock(@NotNull Class<T> responseType) {
        return requestBodySpec
                .exchangeToMono(mapper -> getTransporterDataMono(responseType, mapper))
                .onErrorResume(ErrorHandlers::handleError)
                .onErrorReturn(TransporterData.fail("Failed to return data"))
                .block();
    }

    @Override
    public Flux<DataBuffer> mapToBuffer() {
        return fluxData(DataBuffer.class);
    }

    private <T> Mono<TransporterData<T>> getTransporterDataMono(@NotNull Class<T> responseType, ClientResponse mapper) {
        return MappingChecker.resolveMapping(mapper, responseType, requireNonNull, checkRequiredFields);
    }

    @Override
    public TransporterData<String> transporterData() {
        return requestBodySpec
                .exchangeToMono(mapper ->
                        MappingChecker.resolveMapping(mapper, String.class, requireNonNull, checkRequiredFields))
                .onErrorResume(ErrorHandlers::handleError)
                .block();
    }

    @Override
    public <T> Mono<ResponseEntity<T>> entityData(@NotNull final Class<T> responseType) {
        return requestBodySpec
                .exchangeToMono(mapper ->
                        EntityCheckers.resolveEntityMapping(mapper, responseType, requireNonNull, checkRequiredFields));
    }

    @Override
    public <T> Mono<T> monoData(@NotNull final Class<T> responseType) {
        return requestBodySpec
                .exchangeToMono(mapper ->
                        MonoCheckers.resolveMapping(mapper, responseType, requireNonNull, checkRequiredFields));
    }

    @Override
    public <T> Flux<T> fluxData(@NotNull Class<T> responseType) {
        return requestBodySpec
                .exchangeToFlux(mapper ->
                        FluxCheckers.resolveMapping(mapper, responseType, requireNonNull, checkRequiredFields));
    }

    @Override
    public TransporterReceiver requireNonNull(@Nullable final Boolean enabled) {
        return new DefaultTransporterReceiver(configuration, requestBodySpec,
                Boolean.TRUE.equals(enabled), checkRequiredFields);
    }

    @Override
    public TransporterReceiver checkRequiredFields(@Nullable final Boolean enabled) {
        return new DefaultTransporterReceiver(configuration, requestBodySpec,
                requireNonNull, Boolean.TRUE.equals(enabled));
    }
}

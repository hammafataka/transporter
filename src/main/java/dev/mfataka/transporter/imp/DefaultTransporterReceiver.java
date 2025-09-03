package dev.mfataka.transporter.imp;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

import dev.mfataka.transporter.checkers.EntityCheckers;
import dev.mfataka.transporter.checkers.FluxCheckers;
import dev.mfataka.transporter.checkers.MappingChecker;
import dev.mfataka.transporter.checkers.MonoCheckers;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.handlers.ErrorHandlers;
import dev.mfataka.transporter.model.TransporterData;

/**
 * @author HAMMA FATAKA
 */
@RequiredArgsConstructor(staticName = "of")
public class DefaultTransporterReceiver implements TransporterReceiver {
    private final TransporterConfiguration configuration;
    private final WebClient.RequestBodySpec requestBodySpec;


    @Override
    public <T> Mono<TransporterData<T>> transporterData(@NotNull final Class<T> responseType) {
        return this.requestBodySpec
                .exchangeToMono(mapper -> getTransporterDataMono(responseType, mapper))
                .onErrorResume(ErrorHandlers::handleError)
                .onErrorReturn(TransporterData.fail("Failed to return data"));
    }

    @Override
    public <T> TransporterData<T> transporterDataBlock(@NotNull Class<T> responseType) {
        return this.requestBodySpec
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
        return MappingChecker.resolveMapping(mapper,
                responseType,
                isRequireNonNull(),
                isCheckFields()
        );
    }

    @Override
    public TransporterData<String> transporterData() {
        return this.requestBodySpec
                .exchangeToMono(mapper ->
                        MappingChecker.resolveMapping(mapper,
                                String.class,
                                isRequireNonNull(),
                                isCheckFields()
                        )
                )
                .onErrorResume(ErrorHandlers::handleError)
                .block();
    }

    @Override
    public <T> Mono<ResponseEntity<T>> entityData(@NotNull final Class<T> responseType) {
        return this.requestBodySpec
                .exchangeToMono(mapper ->
                        EntityCheckers.resolveEntityMapping(mapper,
                                responseType,
                                isRequireNonNull(),
                                isCheckFields()
                        )
                );
    }

    @Override
    public <T> Mono<T> monoData(@NotNull final Class<T> responseType) {
        return this.requestBodySpec
                .exchangeToMono(mapper ->
                        MonoCheckers.resolveMapping(mapper,
                                responseType,
                                isRequireNonNull(),
                                isCheckFields()
                        )
                );
    }

    @Override
    public <T> Flux<T> fluxData(@NotNull Class<T> responseType) {
        return this.requestBodySpec
                .exchangeToFlux(mapper ->
                        FluxCheckers.resolveMapping(mapper,
                                responseType,
                                isRequireNonNull(),
                                isCheckFields()
                        )
                );
    }

    private boolean isCheckFields() {
        return configuration.isCheckRequiredFields();
    }

    private boolean isRequireNonNull() {
        return configuration.isRequiredNonNull();
    }

    @Override
    public TransporterReceiver requireNonNull(@Nullable final Boolean enabled) {
        configuration.setRequiredNonNull(Boolean.TRUE.equals(enabled));
        return this;
    }

    @Override
    public TransporterReceiver checkRequiredFields(@Nullable final Boolean enabled) {
        configuration.setCheckRequiredFields(Boolean.TRUE.equals(enabled));
        return this;
    }

}

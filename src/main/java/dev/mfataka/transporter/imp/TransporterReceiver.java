package dev.mfataka.transporter.imp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import dev.mfataka.transporter.model.DemoResponse;
import dev.mfataka.transporter.model.TransporterData;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 14.05.2023 3:47
 */
public interface TransporterReceiver {

    /**
     * method to receive data of client request
     *
     * @param responseType type of expected response of required request
     * @return {@link Mono < TransporterData >} of required response type
     */
    <T> Mono<TransporterData<T>> transporterData(@NotNull final Class<T> responseType);

    /**
     * method to receive data of client request, its blocking operation
     *
     * @param responseType type of expected response of required request
     * @return {@link Mono < TransporterData >} of required response type
     */
    <T> TransporterData<T> transporterDataBlock(@NotNull final Class<T> responseType);

    Flux<DataBuffer> mapToBuffer();

    /**
     * method to receive data of client request, its blocking operation
     *
     * @return {@link TransporterData} of string
     */
    TransporterData<String> transporterData();

    /**
     * method to convert response to {@link Mono<ResponseEntity>}
     *
     * @param responseType type to be converted to
     * @return {@link Mono} of {@link ResponseEntity} of T
     */
    <T> Mono<ResponseEntity<T>> entityData(@NotNull final Class<T> responseType);

    /**
     * method to convert response to {@link Mono}
     *
     * @param responseType type to be converted to
     * @return {@link Mono}  of T
     */
    <T> Mono<T> monoData(@NotNull final Class<T> responseType);

    /**
     * method to convert response to {@link Flux}
     *
     * @param responseType type to be converted to
     * @return {@link Mono}  of T
     */
    <T> Flux<T> fluxData(@NotNull final Class<T> responseType);


    /**
     * if it is called then will check all properties to be not null otherwise will not process it
     *
     * @return {@link TransporterReceiver}.
     */
    TransporterReceiver requireNonNull(@Nullable final Boolean enabled);

    /**
     * It checks if the required fields are present when receiving response.
     *
     * @param enabled true if the required fields should be checked, false otherwise.
     * @return ResponseReceiver for more options.
     * @apiNote you need to document models for jackson mapper see {@link DemoResponse} for more info about documenting it
     */
    TransporterReceiver checkRequiredFields(@Nullable final Boolean enabled);

}

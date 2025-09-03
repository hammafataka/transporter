package dev.mfataka.transporter.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.validation.constraints.NotNull;

import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import io.vavr.control.Try;

import lombok.*;

import dev.mfataka.transporter.handlers.ErrorHandlers;

/**
 * @author HAMMA FATAKA
 */
@Builder
@ToString
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransporterData<D> {
    private final TransportDataResult result;
    private final String resultMessage;
    private final D data;
    private final LocalDateTime dateTime;
    private final HttpStatusCode status;
    private final Throwable throwable;

    /***
     * create ok TransporterData
     * @param data data of TransporterData
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> ok(final Data data) {
        return ok(data, "Operation Succeed", null);
    }

    /***
     * create ok TransporterData
     * @param data data of TransporterData
     * @param resultMessage message of TransporterData
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> ok(final Data data, final String resultMessage) {
        return ok(data, resultMessage, null);
    }

    /***
     * create ok TransporterData
     * @param data data of TransporterData
     * @param status status of TransporterData
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> ok(final Data data, final HttpStatusCode status) {
        return ok(data, "Operation Succeed", status);
    }


    /***
     * create ok TransporterData
     * @param data data of TransporterData
     * @param resultMessage message of TransporterData
     * @param status status of TransporterData
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> ok(final Data data, final String resultMessage, @Nullable HttpStatusCode status) {
        return TransporterData.<Data>builder()
                .result(TransportDataResult.OK)
                .resultMessage(resultMessage)
                .data(data)
                .dateTime(LocalDateTime.now())
                .status(Objects.isNull(status) ? HttpStatusCode.valueOf(200) : status)
                .build();
    }


    /***
     * create fail TransporterData
     * @param resultMessage message of TransporterData, creates throwable from message
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> fail(final String resultMessage) {
        return fail(resultMessage, new Throwable(resultMessage));
    }

    /***
     * create fail TransporterData from errors
     * @param errors errors
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> failWithErrors(final Errors errors) {
        return fail(errors.getAllErrors().toString());
    }

    /***
     * create fail TransporterData
     * @param resultMessage message of TransporterData
     * @param status status of TransporterData
     * @return TransporterData
     */
    public static <Data> TransporterData<Data> failWithStatus(final String resultMessage, final HttpStatus status) {
        return failWithStatus(resultMessage, null, status);
    }

    /***
     * create fail TransporterData
     * @param resultMessage message of TransporterData
     * @param data data of TransporterData
     * @param status status of TransporterData
     * @return TransporterData
     */

    public static <Data> TransporterData<Data> failWithStatus(final String resultMessage, @Nullable final Data data, final HttpStatus status) {
        return TransporterData.<Data>builder()
                .result(TransportDataResult.FAIL)
                .data(data)
                .resultMessage(resultMessage)
                .status(status)
                .dateTime(LocalDateTime.now())
                .build();
    }


    /***
     * create Transporter data from Try
     * @param resultable try result
     * @return TransporterData
     * @param <Data> type of data
     */
    public static <Data> TransporterData<Data> fromTry(final Try<Data> resultable) {
        if (resultable.isFailure()) {
            final Throwable cause = resultable.getCause();
            return TransporterData.fail(cause.getMessage(), cause);
        }
        return TransporterData.ok(resultable.get());
    }


    /***
     * create fail TransporterData
     * @param resultMessage message of TransporterData
     * @param throwable throwable of TransporterData
     * @return TransporterData
     */
    public static <Data> TransporterData<Data> fail(final String resultMessage, final Throwable throwable) {
        return fail(resultMessage, throwable, null);
    }


    /***
     * create fail TransporterData
     * @param resultMessage message of TransporterData
     * @param throwable throwable of TransporterData
     * @param status status of TransporterData
     * @return TransporterData
     */
    public static <Data> TransporterData<Data> fail(final String resultMessage, final Throwable throwable, @Nullable final HttpStatus status) {
        return TransporterData.<Data>builder()
                .result(TransportDataResult.FAIL)
                .resultMessage(resultMessage)
                .throwable(throwable)
                .status(status)
                .dateTime(LocalDateTime.now())
                .build();
    }


    /***
     * create unknown TransporterData
     * @param resultMessage message of TransporterData
     * @return TransporterData
     */
    public static <Data> TransporterData<Data> unknown(final String resultMessage) {
        return unknown(resultMessage, null);
    }

    /***
     * create unknown TransporterData
     * @param resultMessage message of TransporterData
     * @param status status of TransporterData
     * @return TransporterData
     */
    public static <Data> TransporterData<Data> unknown(final String resultMessage, @Nullable HttpStatus status) {
        return TransporterData.<Data>builder()
                .result(TransportDataResult.UNKNOWN)
                .resultMessage(resultMessage)
                .status(status)
                .throwable(new Throwable(resultMessage))
                .dateTime(LocalDateTime.now())
                .build();
    }

    /***
     * map from response entity
     * @param entity response entity
     * @return TransporterData
     */

    public static <Data> TransporterData<Data> fromResponseEntity(final ResponseEntity<Data> entity) {
        if (!entity.getStatusCode().isError()) {
            return ok(entity.getBody(), entity.getStatusCode());
        }
        return ErrorHandlers.handleEntity(entity);
    }

    /***
     * map to response entity
     * @return response entity
     */
    public ResponseEntity<D> asResponseEntity() {
        return new ResponseEntity<>(data, status);
    }

    /***
     * get data from TransporterData
     * @return data from TransporterData if present otherwise throw IllegalStateException
     */
    @NotNull
    public D dataOrThrow() {
        if (isOk()) {
            return data;
        }
        throw new IllegalStateException("TransporterData is not ok");
    }

    /***
     * get data from TransporterData
     * @param fallback data to return when TransporterData is not ok
     * @return data from TransporterData if present otherwise fallback
     */
    @NotNull
    public D dataOr(final D fallback) {
        return isOk() ? data : fallback;
    }

    /***
     * get data from TransporterData
     * @return data from TransporterData if present otherwise null
     */
    @Nullable
    public D data() {
        return data;
    }

    /***
     * check if TransporterData is ok and status is 2xx
     * @return true if TransporterData is ok and status is 2xx
     */
    public boolean isOkAnd2xxStatus() {
        return isOk() && (Objects.nonNull(status) && status.is2xxSuccessful());
    }

    /***
     * check if TransporterData is not ok
     * @return true if TransporterData is not ok
     */
    public boolean isNotOk() {
        return !isOk();
    }

    /***
     * get message of TransporterData if fail otherwise get success message
     * @return message of TransporterData if fail
     */
    public String failMessageWithStatusCheck() {
        if (!isOk()) {
            return resultMessage;
        }
        return "response status code is " + status.value();
    }

    /***
     * check if TransporterData is ok
     * @return true if TransporterData is ok
     */
    public boolean isOk() {
        return result.isOk();
    }

    /***
     * check if TransporterData is fail
     * @return true if TransporterData is fail
     */
    public boolean isFail() {
        return result.isFail();
    }


    /***
     * check if TransporterData is unknown
     * @return true if TransporterData is unknown
     */
    public boolean isUnknown() {
        return result.isUnknown();
    }

    /***
     *  if TransporterData is ok, execute consumer
     * @param consumer consumer to execute
     */
    public void ifOk(final Consumer<D> consumer) {
        if (isOk()) {
            consumer.accept(data);
        }
    }

    /***
     *  if TransporterData is not ok, execute error consumer
     * @param error consumer to execute
     */
    public void ifFail(final Consumer<Throwable> error) {
        if (isFail()) {
            error.accept(throwable);
        }
    }

    /***
     *  map function to data
     * @param mapper function to map
     * @param fallback supplier to return when TransporterData is not ok
     * @return mapped data
     * @param <T> type of data
     */

    @NotNull
    public <T> T map(final Function<D, T> mapper, final Supplier<T> fallback) {
        if (isOk()) {
            return mapper.apply(data);
        }
        return fallback.get();
    }

    /***
     *  map function to data
     * @throws IllegalStateException if TransporterData is not ok
     * @param mapper function to map
     * @return mapped data
     * @param <T> type of data
     */
    @NotNull
    public <T> T map(final Function<D, T> mapper) {
        return map(mapper, () -> {
            throw new IllegalStateException("cannot map when TransporterData is not ok. Result: " + result + " ResultMessage: " + resultMessage);
        });
    }

    public String resultMessage() {
        return resultMessage;
    }

}

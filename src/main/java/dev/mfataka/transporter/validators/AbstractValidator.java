package dev.mfataka.transporter.validators;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import reactor.core.publisher.Mono;


import dev.mfataka.transporter.exceptions.RequiredFieldException;
import dev.mfataka.transporter.model.TransporterData;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 22.07.2022 16:01
 */
public abstract class AbstractValidator implements Validator {

    public <T> Mono<TransporterData<T>> handleRequest(final ResponseEntity<T> responseSpec) {
        return Mono.just(responseSpec)
                .mapNotNull(ResponseEntity::getBody)
                .<TransporterData<T>>flatMap(next -> {
                    final var errors = new BeanPropertyBindingResult(next, next.getClass().getName());
                    validate(next, errors);
                    if (errors.getAllErrors().isEmpty()) {
                        return processBody(next);
                    } else {
                        return onValidationErrorReturn(errors);
                    }
                })
                .switchIfEmpty(Mono.error(new Throwable("Mono produced empty value while validating, possibly null body received")));
    }

    public <T> Mono<T> handleRequireNonNull(final T responseSpec) {
        return Mono.just(responseSpec)
                .flatMap(next -> {
                    final var errors = new BeanPropertyBindingResult(next, next.getClass().getName());
                    validate(next, errors);
                    if (errors.getAllErrors().isEmpty()) {
                        return processMonoBody(next);
                    } else {
                        return onMonoValidationErrorReturn(errors);
                    }
                })
                .switchIfEmpty(Mono.error(new Throwable("Mono produced empty value while validating, possibly null body received")));
    }

    protected <T> Mono<TransporterData<T>> onValidationErrorReturn(final Errors errors) {
        return Mono.just(TransporterData.failWithErrors(errors));
    }

    protected <T> Mono<TransporterData<T>> processBody(T validBody) {
        return Mono.just(TransporterData.ok(validBody));
    }

    public  <T> Mono<T> processMonoBody(T validBody) {
        return Mono.just(validBody);
    }

    protected <T> Mono<T> onMonoValidationErrorReturn(final Errors errors) {
        return Mono.error(new Throwable(errors.getAllErrors().toString()));
    }

    private Exception thrower(Errors errors) {
        return new RequiredFieldException(errors);
    }


}

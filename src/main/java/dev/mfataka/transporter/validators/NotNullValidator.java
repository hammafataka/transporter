package dev.mfataka.transporter.validators;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import reactor.core.publisher.Mono;

import dev.mfataka.transporter.model.TransporterData;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 22.07.2022 15:01
 */
public final class NotNullValidator extends AbstractValidator {

    private final Field[] fields;

    public NotNullValidator(final Class<?> clazz) {
        this.fields = clazz.getDeclaredFields();
    }

    @Override
    public boolean supports(@NotNull final Class<?> clazz) {
        return NotNullValidator.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull final Object target, @NotNull final Errors errors) {
        Arrays.asList(fields)
                .forEach(field -> ValidationUtils.rejectIfEmptyOrWhitespace(errors, field.getName(), field.getName() + " is required"));
    }

    @Override
    protected <T> Mono<TransporterData<T>> processBody(T validBody) {
        return super.processBody(validBody);
    }
}

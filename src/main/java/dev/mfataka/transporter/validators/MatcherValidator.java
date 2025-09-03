package dev.mfataka.transporter.validators;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;


/**
 * @author HAMMA FATAKA
 */
public final class MatcherValidator<T> extends AbstractValidator {

    private final String propertyName;
    private final Field[] fields;
    private String value;

    public MatcherValidator(final String propertyName, final Class<?> clazz) {
        this.propertyName = propertyName;
        this.fields = clazz.getDeclaredFields();
    }


    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return MatcherValidator.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        try {
            final var toCheckField = Arrays.stream(fields)
                    .filter(field -> field.getName().equals(propertyName))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(toCheckField)) {
                value = (String) toCheckField.get(target);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validate(@NotNull String pattern, @NotNull Object target, @NotNull Errors errors) {
        validate(target, errors);
        if (Objects.nonNull(value)) {
            return value.matches(pattern);
        }
        return false;
    }
}

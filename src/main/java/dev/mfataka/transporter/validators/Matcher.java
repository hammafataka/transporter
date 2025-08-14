package dev.mfataka.transporter.validators;

import org.springframework.validation.BeanPropertyBindingResult;


/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 22.07.2022 18:22
 */
public final class Matcher<T> {
    private final T target;

    public Matcher(T target) {
        this.target = target;
    }

    public boolean matches(final String pattern, final String property, final Class<T> clazz) {
        final var matcherValidator = new MatcherValidator<>(property, clazz);
        return matcherValidator.validate(pattern, target, new BeanPropertyBindingResult(target, property));
    }

}

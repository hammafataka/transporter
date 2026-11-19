package dev.mfataka.transporter.exceptions;

import org.springframework.core.NestedRuntimeException;
import org.springframework.validation.Errors;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 22.07.2022 19:27
 */
public class RequiredFieldException extends NestedRuntimeException {

    public RequiredFieldException(String msg) {
        super(msg);
    }

    public RequiredFieldException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RequiredFieldException(Errors errors) {
        super(errors.getAllErrors().toString());
    }

}

package com.indra.asistencia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.ACCEPTED)
public class ValidatedRequestException extends RuntimeException {
    public ValidatedRequestException(String message) {
        super(message);
    }
}

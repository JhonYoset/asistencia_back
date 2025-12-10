package com.indra.asistencia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class RuntimeCustomException extends RuntimeException {
    public RuntimeCustomException(String message) {
        super(message);
    }
}

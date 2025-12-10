package com.indra.asistencia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class ResourceUnAuthorizedException extends RuntimeException {
    public ResourceUnAuthorizedException(String message) {
        super(message);
    }
}

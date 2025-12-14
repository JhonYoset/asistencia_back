package com.indra.asistencia.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;

import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

  private HttpStatus getStatus(Throwable ex) {
    ResponseStatus status = ex.getClass().getAnnotation(ResponseStatus.class);
    if (status != null) {
      return status.value();
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @ExceptionHandler({
      BadRequestException.class,
      ResourceNotFoundException.class,
      ResourceUnAuthorizedException.class,
      ValidatedRequestException.class,
      RuntimeCustomException.class,
      JWTVerificationException.class,
      SignatureVerificationException.class
  })
  public ResponseEntity<ExceptionDto> handleCustomExceptions(RuntimeException ex, HttpServletRequest request) {
    HttpStatus status = getStatus(ex);

    ExceptionDto dto = ExceptionDto.builder()
        .hora(LocalDateTime.now().format(FORMATTER))
        .mensaje(ex.getMessage())
        .url(request.getRequestURI())
        .codeStatus(status.value())
        .build();

    log.error(ex.getClass().getSimpleName() + ": ", ex);

    return ResponseEntity.status(status).body(dto);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionDto> handleValidationException(MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    String mensaje = ex.getBindingResult().getAllErrors().stream()
        .map(error -> {
          if (error instanceof FieldError fe) {
            return fe.getField() + ": " + fe.getDefaultMessage();
          } else {
            return error.getDefaultMessage();
          }
        })
        .collect(Collectors.joining(", "));

    ExceptionDto dto = ExceptionDto.builder()
        .hora(LocalDateTime.now().format(FORMATTER))
        .mensaje(mensaje)
        .url(request.getRequestURI())
        .codeStatus(status.value())
        .build();

    log.error("ValidationException: {}", mensaje);

    return ResponseEntity.status(status).body(dto);
  }


  // @ExceptionHandler(JWTVerificationException.class)
  // public ResponseEntity<?> handleJwtException(JWTVerificationException ex
    
  // ) {

  //     String mensaje = ex.getMessage();
  //     HttpStatus status = HttpStatus.BAD_REQUEST;
  //     ExceptionDto dto = ExceptionDto.builder()
  //         .hora(LocalDateTime.now().format(FORMATTER))
  //         .mensaje(mensaje)
  //         //.url(request.getRequestURI())
  //         .url("")
  //         .codeStatus(status.value())
  //         .build();;
  //     return ResponseEntity.status(status).body(dto);
  // }  
}

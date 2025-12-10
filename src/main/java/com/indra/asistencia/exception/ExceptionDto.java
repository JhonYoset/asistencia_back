package com.indra.asistencia.exception;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDto {
    private String hora;
    private String mensaje;
    private String url;
    private int codeStatus;
}

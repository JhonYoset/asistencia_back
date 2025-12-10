package com.indra.asistencia.dto;

import lombok.*;
import java.time.LocalDate;

@Data @Builder
public class JustificacionRequestDto {
    private LocalDate fecha;
    private String tipo; // TARDANZA o AUSENCIA
    private String motivo;
}
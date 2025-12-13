package com.indra.asistencia.dto;

import lombok.*;
import java.time.LocalDate;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JustificacionRequestDto {
    
    private LocalDate fecha;
    private String tipo;
    private String motivo;
}
package com.indra.asistencia.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class JustificacionResponseDto {
    private Long id;
    private String username;
    private LocalDate fecha;
    private String tipo;
    private String motivo;
    private String estado;
    private String fechaSolicitud;
}

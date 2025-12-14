package com.indra.asistencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JustificacionAdminResponseDto {
    private Long id;
    private String username;
    private String nombreCompleto;
    private LocalDate fecha;
    private String tipo;
    private String motivo;
    private String estado;
    private String fechaSolicitud;
}
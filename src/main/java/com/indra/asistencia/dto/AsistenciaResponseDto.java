package com.indra.asistencia.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class AsistenciaResponseDto {
    private Long id;
    private String nombreEmpleado;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private String estado; // "EN_OFICINA", "COMPLETADO"
    private String fechaRegistro;
}
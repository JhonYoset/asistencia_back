package com.indra.asistencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor // <-- AGREGAR ESTO
@AllArgsConstructor // <-- Y ESTO
public class AsistenciaResponseDto {
    private Long id;
    private String nombreEmpleado;
    private Long usuarioId;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private String estado;
    private String fechaRegistro; // Cambiado a String
}
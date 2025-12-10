package com.indra.asistencia.dto;

import lombok.*;

@Data @Builder
public class AsistenciaRequestDto {
    private String accion; // "CHECKIN" o "CHECKOUT"
}

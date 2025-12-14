package com.indra.asistencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    @NotNull
    private Long id;
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String nombreCompleto;
    
    @NotBlank
    private String rol;
    
    private boolean enabled = true;
    
    @NotNull
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime ultimoAcceso;
    
    private int totalAsistencias = 0;
}
package com.indra.asistencia.dto;
import lombok.*;

@Data 
@Builder
public class CreateUserRequestDto {
    private String username;
    private String password;
    private String nombre;
    private String rol; // "ADMIN" o "EMPLEADO"
}
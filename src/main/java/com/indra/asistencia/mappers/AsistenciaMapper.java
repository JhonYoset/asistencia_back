package com.indra.asistencia.mappers;

import com.indra.asistencia.dto.AsistenciaResponseDto;
import com.indra.asistencia.models.Asistencia;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsistenciaMapper {

    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public AsistenciaResponseDto toResponseDto(Asistencia asistencia) {
        if (asistencia == null) return null;
        
        AsistenciaResponseDto dto = new AsistenciaResponseDto();
        dto.setId(asistencia.getId());
        
        if (asistencia.getUsuario() != null) {
            dto.setNombreEmpleado(asistencia.getUsuario().getUsername());
            dto.setUsuarioId(asistencia.getUsuario().getId());
        }
        
        dto.setEntrada(asistencia.getEntrada());
        dto.setSalida(asistencia.getSalida());
        dto.setEstado(asistencia.getEstado());
        
        // Formatear fechaRegistro si es LocalDate
        if (asistencia.getFechaRegistro() != null) {
            dto.setFechaRegistro(asistencia.getFechaRegistro().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
            ));
        }
        
        return dto;
    }

    public AsistenciaResponseDto toDto(Asistencia asistencia) {
        return toResponseDto(asistencia);
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<AsistenciaResponseDto> toDtoList(List<Asistencia> asistencias) {
        if (asistencias == null || asistencias.isEmpty()) {
            return List.of();
        }
        return asistencias.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Versión con formato completo de fecha y hora (opcional para reportes)
     */
    public AsistenciaResponseDto toDtoWithFullTime(Asistencia asistencia) {
        if (asistencia == null) return null;

        String entradaStr = asistencia.getEntrada() != null 
            ? asistencia.getEntrada().format(FULL_FORMATTER) 
            : null;
        String salidaStr = asistencia.getSalida() != null 
            ? asistencia.getSalida().format(FULL_FORMATTER) 
            : null;

        // CREAR DTO SIN USAR BUILDER
        AsistenciaResponseDto dto = new AsistenciaResponseDto();
        dto.setId(asistencia.getId());
        dto.setNombreEmpleado(asistencia.getUsuario().getUsername());
        dto.setEntrada(asistencia.getEntrada());
        dto.setSalida(asistencia.getSalida());
        
        // Determinar estado
        String estado = asistencia.getEstado();
        if (estado == null) {
            estado = asistencia.getSalida() == null ? "EN_OFICINA" : "COMPLETADO";
        }
        dto.setEstado(estado);
        
        // Campo especial para mostrar formato
        dto.setFechaRegistro(entradaStr + " → " + (salidaStr != null ? salidaStr : "Sin salida"));
        
        return dto;
    }
}
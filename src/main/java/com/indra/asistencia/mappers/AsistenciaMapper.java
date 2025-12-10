package com.indra.asistencia.mappers;

import com.indra.asistencia.dto.AsistenciaResponseDto;
import com.indra.asistencia.models.Asistencia;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsistenciaMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /**
     * Convierte una entidad Asistencia a DTO de respuesta
     */
    public AsistenciaResponseDto toDto(Asistencia asistencia) {
        if (asistencia == null) return null;

        String estado = (asistencia.getSalida() == null) ? "EN_OFICINA" : "COMPLETADO";

        return AsistenciaResponseDto.builder()
                .id(asistencia.getId())
                .nombreEmpleado(asistencia.getUsuario() != null ? asistencia.getUsuario().getUsername() : "Desconocido")
                .entrada(asistencia.getEntrada())
                .salida(asistencia.getSalida())
                .estado(estado)
                .fechaRegistro(
                    asistencia.getFechaRegistro() != null 
                        ? asistencia.getFechaRegistro().format(DATE_FORMATTER) 
                        : null
                )
                .build();
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

        return AsistenciaResponseDto.builder()
                .id(asistencia.getId())
                .nombreEmpleado(asistencia.getUsuario().getUsername())
                .entrada(asistencia.getEntrada())
                .salida(asistencia.getSalida())
                .estado(asistencia.getSalida() == null ? "EN_OFICINA" : "COMPLETADO")
                .fechaRegistro(entradaStr + " → " + (salidaStr != null ? salidaStr : "Sin salida"))
                .build();
    }
}
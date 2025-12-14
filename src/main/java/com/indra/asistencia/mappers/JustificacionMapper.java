package com.indra.asistencia.mappers;

import com.indra.asistencia.dto.JustificacionAdminResponseDto;
import com.indra.asistencia.models.Justificacion;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JustificacionMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public JustificacionAdminResponseDto toAdminDto(Justificacion justificacion) {
        if (justificacion == null) return null;

        return JustificacionAdminResponseDto.builder()
                .id(justificacion.getId())
                .username(justificacion.getUsuario() != null ? justificacion.getUsuario().getUsername() : "N/A")
                .nombreCompleto(justificacion.getUsuario() != null ? justificacion.getUsuario().getNombreCompleto() : "N/A")
                .fecha(justificacion.getFecha())
                .tipo(justificacion.getTipo())
                .motivo(justificacion.getMotivo())
                .estado(justificacion.getEstado())
                .fechaSolicitud(justificacion.getFechaSolicitud() != null 
                    ? justificacion.getFechaSolicitud().format(FORMATTER) 
                    : "N/A")
                .build();
    }

    public List<JustificacionAdminResponseDto> toAdminDtoList(List<Justificacion> justificaciones) {
        if (justificaciones == null || justificaciones.isEmpty()) {
            return List.of();
        }
        
        return justificaciones.stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
    }
}
package com.indra.asistencia.mappers;

import com.indra.asistencia.dto.UserResponseDto;
import com.indra.asistencia.models.User;
import com.indra.asistencia.models.UserRols;
import com.indra.asistencia.models.Asistencia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public UserResponseDto toDto(User user, String rol, int totalAsistencias) {
        if (user == null) return null;

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nombre(user.getUsername()) // Por defecto, usar username como nombre
                .rol(rol != null ? rol : "EMPLEADO")
                .enabled(user.isEnabled())
                .fechaCreacion(java.time.LocalDateTime.now()) // Aquí podrías agregar un campo en la entidad
                .ultimoAcceso(null) // Podrías agregar este campo en la entidad
                .totalAsistencias(totalAsistencias)
                .build();
    }

    public UserResponseDto toDto(User user, List<UserRols> roles, List<Asistencia> asistencias) {
        if (user == null) return null;

        String rol = roles != null && !roles.isEmpty() 
            ? roles.get(0).getRole().getName() 
            : "EMPLEADO";

        int totalAsistencias = asistencias != null ? asistencias.size() : 0;

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nombre(user.getUsername()) // Por defecto
                .rol(rol)
                .enabled(user.isEnabled())
                .fechaCreacion(java.time.LocalDateTime.now())
                .ultimoAcceso(null)
                .totalAsistencias(totalAsistencias)
                .build();
    }

    public List<UserResponseDto> toDtoList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        
        return users.stream()
                .map(user -> toDto(user, "EMPLEADO", 0))
                .collect(Collectors.toList());
    }
}
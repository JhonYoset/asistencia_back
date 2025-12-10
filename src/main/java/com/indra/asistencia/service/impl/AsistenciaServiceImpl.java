package com.indra.asistencia.service.impl;

import com.indra.asistencia.dto.*;
import com.indra.asistencia.exception.ResourceNotFoundException;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.models.*;
import com.indra.asistencia.repository.*;
import com.indra.asistencia.service.IAsistenciaService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final IUserRepository userRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final JustificacionRepository justificacionRepo;

    @Override
    public String registrarAsistencia(String username, String accion) {
        User usuario = userRepo.getByUserName(username)
            .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));

        LocalDate hoy = LocalDate.now();
        Asistencia ultima = asistenciaRepo
            .findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(usuario, hoy)
            .orElse(null);

        if ("CHECKIN".equalsIgnoreCase(accion)) {
            if (ultima != null && ultima.getSalida() == null) {
                throw new ValidatedRequestException("Ya tienes un check-in sin cerrar hoy");
            }
            Asistencia nueva = Asistencia.builder()
                .usuario(usuario)
                .entrada(LocalDateTime.now())
                .build();
            asistenciaRepo.save(nueva);
            return "Check-in registrado correctamente";
        }

        if ("CHECKOUT".equalsIgnoreCase(accion)) {
            if (ultima == null || ultima.getSalida() != null) {
                throw new ValidatedRequestException("No tienes check-in abierto hoy");
            }
            ultima.setSalida(LocalDateTime.now());
            asistenciaRepo.save(ultima);
            return "Check-out registrado correctamente";
        }

        throw new ValidatedRequestException("Acción no válida");
    }

    @Override
    public List<AsistenciaResponseDto> getHistorial(String username) {
        User usuario = userRepo.getByUserName(username).get();
        return asistenciaRepo.findAll().stream()
            .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
            .map(a -> AsistenciaResponseDto.builder()
                .id(a.getId())
                .nombreEmpleado(usuario.getUsername())
                .entrada(a.getEntrada())
                .salida(a.getSalida())
                .estado(a.getSalida() == null ? "EN_OFICINA" : "COMPLETADO")
                .fechaRegistro(a.getFechaRegistro().toString())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public JustificacionResponseDto solicitarJustificacion(String username, JustificacionRequestDto dto) {
        User usuario = userRepo.getByUserName(username)
            .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));

        Justificacion j = Justificacion.builder()
            .usuario(usuario)
            .fecha(dto.getFecha())
            .tipo(dto.getTipo().toUpperCase())
            .motivo(dto.getMotivo())
            .estado("PENDIENTE")
            .build();

        justificacionRepo.save(j);

        return JustificacionResponseDto.builder()
            .id(j.getId())
            .username(usuario.getUsername())
            .fecha(j.getFecha())
            .tipo(j.getTipo())
            .motivo(j.getMotivo())
            .estado(j.getEstado())
            .fechaSolicitud(j.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
            .build();
    }
    @Override
    public List<Justificacion> getJustificacionesPendientes() {
        return justificacionRepo.findByEstado("PENDIENTE");
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String aprobarJustificacion(Long id) {
        Justificacion j = justificacionRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Justificación no encontrada"));

        if (!"PENDIENTE".equals(j.getEstado())) {
            throw new ValidatedRequestException("Esta justificación ya fue procesada");
        }

        j.setEstado("APROBADO");
        justificacionRepo.save(j);
        return "Justificación aprobada correctamente";
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<AsistenciaResponseDto> reportePorRangoFechas(LocalDate desde, LocalDate hasta) {
        return asistenciaRepo.findAll().stream()
            .filter(a -> !a.getFechaRegistro().isBefore(desde) && !a.getFechaRegistro().isAfter(hasta))
            .map(a -> AsistenciaResponseDto.builder()
                .id(a.getId())
                .nombreEmpleado(a.getUsuario().getUsername())
                .entrada(a.getEntrada())
                .salida(a.getSalida())
                .estado(a.getSalida() == null ? "EN_OFICINA" : "COMPLETADO")
                .fechaRegistro(a.getFechaRegistro().toString())
                .build())
            .collect(Collectors.toList());
}
}
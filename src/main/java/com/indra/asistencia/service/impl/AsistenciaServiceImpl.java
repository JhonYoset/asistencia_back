package com.indra.asistencia.service.impl;

import com.indra.asistencia.dto.*;
import com.indra.asistencia.exception.ResourceNotFoundException;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.mappers.AsistenciaMapper;
import com.indra.asistencia.models.*;
import com.indra.asistencia.repository.*;
import com.indra.asistencia.service.IAsistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final AsistenciaMapper asistenciaMapper;

    private static final LocalTime HORA_LIMITE_TARDANZA = LocalTime.of(9, 10); // 09:10 AM

    @Override
    public String registrarAsistencia(String username, String accion) {
        User usuario = userRepo.getByUserName(username)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado o inactivo"));

        LocalDate hoy = LocalDate.now();
        Asistencia ultima = asistenciaRepo
                .findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(usuario, hoy)
                .orElse(null);

        LocalDateTime ahora = LocalDateTime.now();

        if ("CHECKIN".equalsIgnoreCase(accion)) {
            if (ultima != null && ultima.getSalida() == null) {
                throw new ValidatedRequestException("Ya tienes un check-in sin cerrar hoy");
            }

            Asistencia nueva = Asistencia.builder()
                    .usuario(usuario)
                    .entrada(ahora)
                    .build();

            asistenciaRepo.save(nueva);

            // TOLERANCIA DE 10 MINUTOS → si llega después de las 09:10, se crea justificación automática
            if (ahora.toLocalTime().isAfter(HORA_LIMITE_TARDANZA)) {
                Justificacion tardanza = Justificacion.builder()
                        .usuario(usuario)
                        .fecha(hoy)
                        .tipo("TARDANZA")
                        .motivo("Llegada después de las 09:10 - Sistema automático")
                        .estado("PENDIENTE")
                        .build();
                justificacionRepo.save(tardanza);
            }

            return "Check-in registrado correctamente";
        }

        if ("CHECKOUT".equalsIgnoreCase(accion)) {
            if (ultima == null || ultima.getSalida() != null) {
                throw new ValidatedRequestException("No tienes check-in abierto hoy");
            }

            ultima.setSalida(ahora);
            asistenciaRepo.save(ultima);
            return "Check-out registrado correctamente";
        }

        throw new ValidatedRequestException("Acción no válida. Usa CHECKIN o CHECKOUT");
    }

    @Override
    public List<AsistenciaResponseDto> getHistorial(String username) {
        User usuario = userRepo.getByUserName(username)
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));

        return asistenciaRepo.findAll().stream()
                .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                .map(asistenciaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public JustificacionResponseDto solicitarJustificacion(String username, JustificacionRequestDto dto) {
        User usuario = userRepo.getByUserName(username)
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));

        Justificacion justificacion = Justificacion.builder()
                .usuario(usuario)
                .fecha(dto.getFecha())
                .tipo(dto.getTipo() != null ? dto.getTipo().toUpperCase() : "TARDANZA")
                .motivo(dto.getMotivo())
                .estado("PENDIENTE")
                .build();

        justificacionRepo.save(justificacion);

        return JustificacionResponseDto.builder()
                .id(justificacion.getId())
                .username(usuario.getUsername())
                .fecha(justificacion.getFecha())
                .tipo(justificacion.getTipo())
                .motivo(justificacion.getMotivo())
                .estado(justificacion.getEstado())
                .fechaSolicitud(justificacion.getFechaSolicitud()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
        if (desde.isAfter(hasta)) {
            throw new ValidatedRequestException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        return asistenciaRepo.findAll().stream()
                .filter(a -> {
                    LocalDate fecha = a.getFechaRegistro();
                    return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                })
                .map(asistenciaMapper::toDto)
                .collect(Collectors.toList());
    }
}
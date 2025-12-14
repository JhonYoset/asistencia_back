package com.indra.asistencia.service.impl;

import com.indra.asistencia.dto.JustificacionRequestDto;
import com.indra.asistencia.dto.JustificacionResponseDto;
import com.indra.asistencia.exception.ResourceNotFoundException;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.models.Justificacion;
import com.indra.asistencia.models.User;
import com.indra.asistencia.repository.IUserRepository;
import com.indra.asistencia.repository.JustificacionRepository;
import com.indra.asistencia.service.IJustificacionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JustificacionServiceImpl implements IJustificacionService {

    private static final Logger logger = LoggerFactory.getLogger(JustificacionServiceImpl.class);
    
    private final JustificacionRepository justificacionRepo;
    private final IUserRepository userRepo;

    @Override
    public JustificacionResponseDto solicitarJustificacion(String username, JustificacionRequestDto dto) {
        logger.info("=== SOLICITAR JUSTIFICACIÓN ===");
        logger.info("Usuario: {}", username);
        
        User usuario = userRepo.getByUserName(username)
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));

        if (dto.getFecha() == null) {
            throw new ValidatedRequestException("La fecha es obligatoria");
        }
        
        if (dto.getMotivo() == null || dto.getMotivo().trim().isEmpty()) {
            throw new ValidatedRequestException("El motivo es obligatorio");
        }
        
        if (dto.getMotivo().length() < 10) {
            throw new ValidatedRequestException("El motivo debe tener al menos 10 caracteres");
        }

        String tipo = (dto.getTipo() != null && !dto.getTipo().trim().isEmpty()) 
                    ? dto.getTipo().toUpperCase() 
                    : "TARDANZA";
        
        if (!tipo.equals("TARDANZA") && !tipo.equals("AUSENCIA")) {
            throw new ValidatedRequestException("Tipo inválido. Use TARDANZA o AUSENCIA");
        }

        Justificacion justificacion = Justificacion.builder()
                .usuario(usuario)
                .fecha(dto.getFecha())
                .tipo(tipo)
                .motivo(dto.getMotivo().trim())
                .estado("PENDIENTE")
                .fechaSolicitud(LocalDateTime.now())
                .build();

        justificacionRepo.save(justificacion);
        
        logger.info("✅ Justificación guardada - ID: {}", justificacion.getId());

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
        logger.info("=== OBTENIENDO JUSTIFICACIONES PENDIENTES ===");
        List<Justificacion> pendientes = justificacionRepo.findByEstado("PENDIENTE");
        logger.info("✅ Encontradas {} justificaciones pendientes", pendientes.size());
        return pendientes;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String aprobarJustificacion(Long id) {
        logger.info("=== APROBAR JUSTIFICACIÓN ===");
        logger.info("ID: {}", id);
        
        Justificacion justificacion = justificacionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Justificación no encontrada con ID: " + id));

        if (!"PENDIENTE".equals(justificacion.getEstado())) {
            throw new ValidatedRequestException("Esta justificación ya fue procesada. Estado actual: " + justificacion.getEstado());
        }

        justificacion.setEstado("APROBADO");
        justificacionRepo.save(justificacion);
        
        logger.info("✅ Justificación aprobada correctamente");
        
        return "Justificación aprobada correctamente";
    }

    @Override
    public List<Justificacion> getMisJustificaciones(String username) {
        logger.info("=== OBTENIENDO MIS JUSTIFICACIONES ===");
        
        User usuario = userRepo.getByUserName(username)
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));

        List<Justificacion> misJustificaciones = justificacionRepo.findAll().stream()
                .filter(j -> j.getUsuario().getId().equals(usuario.getId()))
                .toList();
        
        logger.info("✅ Encontradas {} justificaciones", misJustificaciones.size());
        
        return misJustificaciones;
    }
}
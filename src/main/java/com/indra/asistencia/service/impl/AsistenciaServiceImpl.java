package com.indra.asistencia.service.impl;

import com.indra.asistencia.dto.*;
import com.indra.asistencia.exception.ResourceNotFoundException;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.mappers.AsistenciaMapper;
import com.indra.asistencia.models.*;
import com.indra.asistencia.repository.*;
import com.indra.asistencia.service.IAsistenciaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AsistenciaServiceImpl.class);
    
    private final IUserRepository userRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final JustificacionRepository justificacionRepo;
    private final AsistenciaMapper asistenciaMapper;

    private static final LocalTime HORA_LIMITE_TARDANZA = LocalTime.of(9, 10);

    @Override
    public String registrarAsistencia(String username, String accion) {
        logger.info("=== REGISTRO DE ASISTENCIA ===");
        logger.info("Usuario: {}", username);
        logger.info("Acción: {}", accion);
        
        User usuario = userRepo.getByUserName(username)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado o inactivo"));

        logger.info("Usuario encontrado - ID: {}, Username: {}", usuario.getId(), usuario.getUsername());

        LocalDate hoy = LocalDate.now();
        logger.info("Fecha de hoy: {}", hoy);
        
        // ✅ INTENTAR MÚLTIPLES QUERIES
        List<Asistencia> asistenciasHoy = null;
        
        // Intento 1: Query con objeto User
        try {
            asistenciasHoy = asistenciaRepo.findByUsuarioAndFechaRegistro(usuario, hoy);
            logger.info("Intento 1 (JPQL con User) - Encontradas: {}", asistenciasHoy.size());
        } catch (Exception e) {
            logger.error("Error en Intento 1: {}", e.getMessage());
        }
        
        // Intento 2: Query con ID del usuario
        if (asistenciasHoy == null || asistenciasHoy.isEmpty()) {
            try {
                asistenciasHoy = asistenciaRepo.findAllByUsuarioIdAndFecha(usuario.getId(), hoy);
                logger.info("Intento 2 (JPQL con ID) - Encontradas: {}", asistenciasHoy.size());
            } catch (Exception e) {
                logger.error("Error en Intento 2: {}", e.getMessage());
            }
        }
        
        // Intento 3: Native Query
        if (asistenciasHoy == null || asistenciasHoy.isEmpty()) {
            try {
                asistenciasHoy = asistenciaRepo.findByUsuarioIdAndFechaNative(usuario.getId(), hoy);
                logger.info("Intento 3 (Native SQL) - Encontradas: {}", asistenciasHoy.size());
            } catch (Exception e) {
                logger.error("Error en Intento 3: {}", e.getMessage());
            }
        }
        
        // Intento 4: Buscar manualmente en TODAS las asistencias
        if (asistenciasHoy == null || asistenciasHoy.isEmpty()) {
            logger.warn("⚠️ Usando búsqueda manual en memoria...");
            asistenciasHoy = asistenciaRepo.findAll().stream()
                    .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                    .filter(a -> a.getFechaRegistro() != null && a.getFechaRegistro().equals(hoy))
                    .sorted((a1, a2) -> a2.getEntrada().compareTo(a1.getEntrada()))
                    .collect(Collectors.toList());
            logger.info("Intento 4 (Manual) - Encontradas: {}", asistenciasHoy.size());
        }
        
        if (asistenciasHoy != null && !asistenciasHoy.isEmpty()) {
            logger.info("✅ Total asistencias de hoy: {}", asistenciasHoy.size());
            asistenciasHoy.forEach(a -> {
                logger.info("  → ID: {}, Entrada: {}, Salida: {}, Estado: {}", 
                           a.getId(), a.getEntrada(), a.getSalida(), a.getEstado());
            });
        }
        
        // Tomar la primera (más reciente)
        Asistencia ultima = (asistenciasHoy != null && !asistenciasHoy.isEmpty()) ? asistenciasHoy.get(0) : null;

        LocalDateTime ahora = LocalDateTime.now();
        logger.info("Hora actual: {}", ahora);

        if ("CHECKIN".equalsIgnoreCase(accion)) {
            logger.info("📍 Procesando CHECK-IN...");
            
            if (ultima != null && ultima.getSalida() == null) {
                logger.warn("⚠️ Ya existe check-in sin cerrar - ID: {}", ultima.getId());
                throw new ValidatedRequestException("Ya tienes un check-in sin cerrar hoy");
            }

            Asistencia nueva = Asistencia.builder()
                    .usuario(usuario)
                    .entrada(ahora)
                    .fechaRegistro(hoy)
                    .estado("EN_OFICINA")
                    .build();

            Asistencia guardada = asistenciaRepo.save(nueva);
            asistenciaRepo.flush(); // Forzar guardado
            
            logger.info("✅ CHECK-IN guardado - ID: {}", guardada.getId());
            
            // Verificar que se guardó correctamente
            Asistencia verificacion = asistenciaRepo.findById(guardada.getId()).orElse(null);
            if (verificacion != null) {
                logger.info("✅ VERIFICACIÓN: Registro encontrado en BD");
                logger.info("   ID: {}, Entrada: {}, FechaRegistro: {}", 
                           verificacion.getId(), verificacion.getEntrada(), verificacion.getFechaRegistro());
            } else {
                logger.error("❌ VERIFICACIÓN: NO se encontró el registro en BD");
            }

            if (ahora.toLocalTime().isAfter(HORA_LIMITE_TARDANZA)) {
                logger.info("⏰ Tardanza detectada");
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
            logger.info("📍 Procesando CHECK-OUT...");
            
            if (ultima == null) {
                logger.error("❌❌❌ NO SE ENCONTRÓ CHECK-IN DE HOY ❌❌❌");
                logger.error("   Usuario ID: {}", usuario.getId());
                logger.error("   Fecha buscada: {}", hoy);
                
                // Mostrar TODOS los registros de asistencia
                List<Asistencia> todas = asistenciaRepo.findAll();
                logger.error("   Total registros ASISTENCIA: {}", todas.size());
                
                todas.stream()
                    .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                    .forEach(a -> {
                        logger.error("   → ID: {}, FechaRegistro: {}, Entrada: {}, Salida: {}", 
                                   a.getId(), a.getFechaRegistro(), a.getEntrada(), a.getSalida());
                    });
                
                throw new ValidatedRequestException("No tienes check-in abierto hoy");
            }
            
            if (ultima.getSalida() != null) {
                logger.warn("⚠️ Ya tiene salida: {}", ultima.getSalida());
                throw new ValidatedRequestException("Ya registraste tu salida hoy");
            }

            logger.info("✅ Actualizando ID: {}", ultima.getId());
            ultima.setSalida(ahora);
            ultima.setEstado("COMPLETADO");
            
            Asistencia actualizada = asistenciaRepo.save(ultima);
            asistenciaRepo.flush();
            
            logger.info("✅ CHECK-OUT guardado - ID: {}, Salida: {}, Estado: {}", 
                       actualizada.getId(), actualizada.getSalida(), actualizada.getEstado());
            
            return "Check-out registrado correctamente";
        }

        throw new ValidatedRequestException("Acción no válida");
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
        return asistenciaRepo.findAll().stream()
            .filter(asistencia -> {
                if (asistencia.getFechaRegistro() == null) {
                    return false;
                }
                return !asistencia.getFechaRegistro().isBefore(desde) && 
                       !asistencia.getFechaRegistro().isAfter(hasta);
            })
            .map(asistenciaMapper::toDto)
            .collect(Collectors.toList());
    }
}
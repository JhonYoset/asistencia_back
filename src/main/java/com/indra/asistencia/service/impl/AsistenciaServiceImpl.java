package com.indra.asistencia.service.impl;

import com.indra.asistencia.dto.AsistenciaResponseDto;
import com.indra.asistencia.dto.JustificacionRequestDto;
import com.indra.asistencia.dto.JustificacionResponseDto;
import com.indra.asistencia.exception.ResourceNotFoundException;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.mappers.AsistenciaMapper;
import com.indra.asistencia.models.Asistencia;
import com.indra.asistencia.models.Justificacion;
import com.indra.asistencia.models.User;
import com.indra.asistencia.repository.AsistenciaRepository;
import com.indra.asistencia.repository.IUserRepository;
import com.indra.asistencia.repository.JustificacionRepository;
import com.indra.asistencia.service.IAsistenciaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        LocalDateTime ahora = LocalDateTime.now();
        
        logger.info("Fecha de hoy: {}", hoy);
        logger.info("Hora actual: {}", ahora);
        
        // ✅ BUSCAR ASISTENCIAS DE HOY - MÉTODO MEJORADO
        List<Asistencia> todasAsistencias = asistenciaRepo.findAll();
        logger.info("Total de asistencias en BD: {}", todasAsistencias.size());
        
        // Filtrar manualmente por usuario y fecha
        List<Asistencia> asistenciasHoy = todasAsistencias.stream()
                .filter(a -> {
                    boolean esDelUsuario = a.getUsuario().getId().equals(usuario.getId());
                    boolean esFechaHoy = a.getFechaRegistro() != null && a.getFechaRegistro().equals(hoy);
                    
                    if (esDelUsuario) {
                        logger.info("Asistencia encontrada - ID: {}, FechaRegistro: {}, Entrada: {}, Salida: {}", 
                                   a.getId(), a.getFechaRegistro(), a.getEntrada(), a.getSalida());
                    }
                    
                    return esDelUsuario && esFechaHoy;
                })
                .sorted((a1, a2) -> {
                    // Ordenar por entrada (más reciente primero)
                    if (a1.getEntrada() == null) return 1;
                    if (a2.getEntrada() == null) return -1;
                    return a2.getEntrada().compareTo(a1.getEntrada());
                })
                .collect(Collectors.toList());
        
        logger.info("Asistencias de hoy encontradas: {}", asistenciasHoy.size());
        
        // Tomar la más reciente
        Asistencia ultima = asistenciasHoy.isEmpty() ? null : asistenciasHoy.get(0);
        
        if (ultima != null) {
            logger.info("✅ Última asistencia - ID: {}, Entrada: {}, Salida: {}, Estado: {}", 
                       ultima.getId(), ultima.getEntrada(), ultima.getSalida(), ultima.getEstado());
        } else {
            logger.info("⚠️ No se encontró asistencia de hoy");
        }

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
            asistenciaRepo.flush();
            
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

            // Verificar tardanza
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
                logger.error("   Total asistencias del usuario en BD: {}", 
                           todasAsistencias.stream()
                               .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                               .count());
                
                // Mostrar TODAS las asistencias del usuario para debugging
                todasAsistencias.stream()
                    .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                    .forEach(a -> {
                        logger.error("   → ID: {}, FechaRegistro: {}, Entrada: {}, Salida: {}, Estado: {}", 
                                   a.getId(), a.getFechaRegistro(), a.getEntrada(), a.getSalida(), a.getEstado());
                    });
                
                throw new ValidatedRequestException("No tienes check-in abierto hoy");
            }
            
            if (ultima.getSalida() != null) {
                logger.warn("⚠️ Ya tiene salida: {}", ultima.getSalida());
                throw new ValidatedRequestException("Ya registraste tu salida hoy");
            }

            logger.info("✅ Actualizando registro ID: {}", ultima.getId());
            logger.info("   Estado actual: {}", ultima.getEstado());
            logger.info("   Entrada: {}", ultima.getEntrada());
            logger.info("   Salida actual: {}", ultima.getSalida());
            
            // Actualizar la salida
            ultima.setSalida(ahora);
            ultima.setEstado("COMPLETADO");
            
            Asistencia actualizada = asistenciaRepo.save(ultima);
            asistenciaRepo.flush();
            
            logger.info("✅ CHECK-OUT guardado exitosamente");
            logger.info("   ID: {}", actualizada.getId());
            logger.info("   Salida: {}", actualizada.getSalida());
            logger.info("   Estado: {}", actualizada.getEstado());
            
            // Verificar que se actualizó correctamente
            Asistencia verificacion = asistenciaRepo.findById(actualizada.getId()).orElse(null);
            if (verificacion != null) {
                logger.info("✅ VERIFICACIÓN: Salida registrada en BD: {}", verificacion.getSalida());
            } else {
                logger.error("❌ VERIFICACIÓN: NO se encontró el registro actualizado");
            }
            
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
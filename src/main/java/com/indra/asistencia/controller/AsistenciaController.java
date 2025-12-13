package com.indra.asistencia.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.indra.asistencia.dto.*;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.models.Asistencia;
import com.indra.asistencia.models.User;
import com.indra.asistencia.repository.AsistenciaRepository;
import com.indra.asistencia.repository.IUserRepository;
import com.indra.asistencia.service.IAsistenciaService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
public class AsistenciaController {

    private static final Logger logger = LoggerFactory.getLogger(AsistenciaController.class);
    private final IAsistenciaService service;
    private final IUserRepository userRepository;
    private final AsistenciaRepository asistenciaRepository;

    @PostMapping("/checkin")
    public ResponseEntity<String> checkin(Authentication auth) {
        return ResponseEntity.ok(service.registrarAsistencia(auth.getName(), "CHECKIN"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(Authentication auth) {
        return ResponseEntity.ok(service.registrarAsistencia(auth.getName(), "CHECKOUT"));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<AsistenciaResponseDto>> historial(Authentication auth) {
        return ResponseEntity.ok(service.getHistorial(auth.getName()));
    }

    /**
     * ✅ ENDPOINT SIMPLE PARA VERIFICAR ESTADO (Sin DTO adicional)
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> getEstadoActual(Authentication auth) {
        Map<String, Object> estado = new HashMap<>();
        
        try {
            User usuario = userRepository.getByUserName(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            LocalDate hoy = LocalDate.now();
            Asistencia ultima = asistenciaRepository
                    .findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(usuario, hoy)
                    .orElse(null);
            
            if (ultima == null) {
                estado.put("enOficina", false);
                estado.put("mensaje", "No has registrado check-in hoy");
                estado.put("ultimaEntrada", null);
                estado.put("ultimaSalida", null);
            } else {
                boolean enOficina = (ultima.getSalida() == null);
                estado.put("enOficina", enOficina);
                estado.put("mensaje", enOficina ? "Estás en la oficina" : "Ya hiciste check-out hoy");
                estado.put("ultimaEntrada", ultima.getEntrada());
                estado.put("ultimaSalida", ultima.getSalida());
            }
        } catch (Exception e) {
            estado.put("enOficina", false);
            estado.put("mensaje", "Error al obtener estado");
            estado.put("ultimaEntrada", null);
            estado.put("ultimaSalida", null);
        }
        
        return ResponseEntity.ok(estado);
    }

    @PostMapping("/justificaciones_solicitud")
    public ResponseEntity<JustificacionResponseDto> solicitarJustificacion(
            Authentication auth,
            @RequestBody JustificacionRequestDto dto) {
        
        logger.info("=== INICIO - Solicitud de Justificación ===");
        logger.info("Usuario: {}", auth.getName());
        logger.info("DTO recibido: {}", dto);
        logger.info("Fecha: {}", dto.getFecha());
        logger.info("Tipo: {}", dto.getTipo());
        logger.info("Motivo: {}", dto.getMotivo());
        
        try {
            JustificacionResponseDto response = service.solicitarJustificacion(auth.getName(), dto);
            logger.info("Justificación creada exitosamente con ID: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al procesar justificación", e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/justificaciones_aprobacion/{id}")
    public ResponseEntity<String> aprobarJustificacion(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprobarJustificacion(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/fechas")
    public ResponseEntity<List<AsistenciaResponseDto>> reportePorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(service.reportePorRangoFechas(desde, hasta));
    }

    @GetMapping("/estado-actual")
    public ResponseEntity<Map<String, Object>> estadoActual(Authentication auth) {
        User usuario = userRepository.getByUserName(auth.getName())
                .orElseThrow(() -> new ValidatedRequestException("Usuario no encontrado"));
        
        LocalDate hoy = LocalDate.now();
        Optional<Asistencia> asistenciaHoy = asistenciaRepository
                .findFirstByUsuarioAndFechaRegistroOrderByEntradaDesc(usuario, hoy);
        
        Map<String, Object> estado = new HashMap<>();
        
        if (asistenciaHoy.isPresent()) {
            Asistencia asistencia = asistenciaHoy.get();
            estado.put("enOficina", asistencia.getSalida() == null);
            estado.put("ultimaEntrada", asistencia.getEntrada());
            estado.put("ultimaSalida", asistencia.getSalida());
            estado.put("fechaRegistro", asistencia.getFechaRegistro());
        } else {
            estado.put("enOficina", false);
            estado.put("ultimaEntrada", null);
            estado.put("ultimaSalida", null);
            estado.put("fechaRegistro", null);
        }
        
        return ResponseEntity.ok(estado);
    }
}
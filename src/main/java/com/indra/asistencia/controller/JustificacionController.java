package com.indra.asistencia.controller;

import com.indra.asistencia.dto.JustificacionRequestDto;
import com.indra.asistencia.dto.JustificacionResponseDto;
import com.indra.asistencia.models.Justificacion;
import com.indra.asistencia.service.IJustificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencia/justificacion")
@RequiredArgsConstructor
public class JustificacionController {

    private static final Logger logger = LoggerFactory.getLogger(JustificacionController.class);
    
    private final IJustificacionService justificacionService;

    /**
     * Solicitar una nueva justificación (empleado)
     */
    @PostMapping
    public ResponseEntity<JustificacionResponseDto> solicitarJustificacion(
            Authentication auth,
            @Valid @RequestBody JustificacionRequestDto dto) {
        
        logger.info("POST /api/asistencia/justificacion - Usuario: {}", auth.getName());
        
        JustificacionResponseDto response = justificacionService.solicitarJustificacion(auth.getName(), dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Aprobar justificación (admin) - ✅ ESTE ES EL ENDPOINT QUE FALTABA
     */
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<String> aprobarJustificacion(@PathVariable Long id) {
        logger.info("POST /api/asistencia/justificacion/{}/aprobar", id);
        
        String mensaje = justificacionService.aprobarJustificacion(id);
        return ResponseEntity.ok(mensaje);
    }

    /**
     * Obtener mis justificaciones (empleado)
     */
    @GetMapping("/mis-justificaciones")
    public ResponseEntity<List<Justificacion>> getMisJustificaciones(Authentication auth) {
        logger.info("GET /api/asistencia/justificacion/mis-justificaciones - Usuario: {}", auth.getName());
        
        List<Justificacion> justificaciones = justificacionService.getMisJustificaciones(auth.getName());
        return ResponseEntity.ok(justificaciones);
    }
}
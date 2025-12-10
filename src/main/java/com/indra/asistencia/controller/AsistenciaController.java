package com.indra.asistencia.controller;

import com.indra.asistencia.dto.*;
import com.indra.asistencia.models.Justificacion;
import com.indra.asistencia.service.IAsistenciaService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
@RequiredArgsConstructor
public class AsistenciaController {

    private final IAsistenciaService service;

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
/* 
    @PostMapping("/justificacion")
    public ResponseEntity<String> justificar(Authentication auth, @RequestBody JustificacionRequestDto dto) {
        return ResponseEntity.ok(service.solicitarJustificacion(auth.getName(), dto));
    }*/

    @PostMapping("/justificaciones_solicitud")
    public ResponseEntity<JustificacionResponseDto> solicitarJustificacion(
            Authentication auth,
            @RequestBody JustificacionRequestDto dto) {
        return ResponseEntity.ok(service.solicitarJustificacion(auth.getName(), dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/justificaciones_aprobacion/{id}")
    public ResponseEntity<String> aprobarJustificacion(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprobarJustificacion(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reportes/fechas")
    public ResponseEntity<Object> reporteFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(service.reportePorRangoFechas(desde, hasta));
    
}
}
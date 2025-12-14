package com.indra.asistencia.controller;

import com.indra.asistencia.dto.CreateUserRequestDto;
import com.indra.asistencia.dto.JustificacionAdminResponseDto;
import com.indra.asistencia.dto.UserResponseDto;
import com.indra.asistencia.mappers.JustificacionMapper;
import com.indra.asistencia.models.Justificacion;
import com.indra.asistencia.repository.JustificacionRepository;
import com.indra.asistencia.service.IUserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final IUserAdminService userAdminService;
    private final JustificacionRepository justificacionRepository;
    private final JustificacionMapper justificacionMapper;


    @GetMapping("/usuarios")
    public ResponseEntity<List<UserResponseDto>> listarUsuarios() {
        List<UserResponseDto> usuarios = userAdminService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<String> crearUsuario(@Valid @RequestBody CreateUserRequestDto dto) {
        String resultado = userAdminService.crearUsuario(dto);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UserResponseDto> obtenerUsuario(@PathVariable Long id) {
        UserResponseDto usuario = userAdminService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequestDto dto) {
        String resultado = userAdminService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/usuarios/{id}/desactivar")
    public ResponseEntity<String> desactivarUsuario(@PathVariable Long id) {
        String resultado = userAdminService.desactivarUsuario(id);
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/usuarios/{id}/activar")
    public ResponseEntity<String> activarUsuario(@PathVariable Long id) {
        String resultado = userAdminService.activarUsuario(id);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener estadísticas del dashboard
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Total de usuarios
        List<UserResponseDto> usuarios = userAdminService.listarUsuarios();
        estadisticas.put("totalEmpleados", usuarios.size());
        
        // Presentes hoy (simplificado - puedes mejorarlo)
        estadisticas.put("presentesHoy", 0);
        
        // Justificaciones pendientes
        List<Justificacion> pendientes = justificacionRepository.findByEstado("PENDIENTE");
        estadisticas.put("justificacionesPendientes", pendientes.size());
        
        // Porcentaje de puntualidad (simplificado)
        estadisticas.put("porcentajePuntualidad", 85);
        
        // Datos para gráfico de barras (últimos 7 días)
        estadisticas.put("dias", List.of("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"));
        estadisticas.put("asistenciasPorDia", List.of(12, 15, 14, 18, 16, 8, 5));
        
        // Datos para gráfico de dona
        estadisticas.put("puntuales", 45);
        estadisticas.put("tardanzas", 8);
        estadisticas.put("ausencias", 3);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtener justificaciones pendientes
     */
    @GetMapping("/justificaciones/pendientes")
    public ResponseEntity<List<JustificacionAdminResponseDto>> getJustificacionesPendientes() {
        List<Justificacion> pendientes = justificacionRepository.findByEstado("PENDIENTE");
        List<JustificacionAdminResponseDto> dtos = justificacionMapper.toAdminDtoList(pendientes);
        return ResponseEntity.ok(dtos);
    }
}
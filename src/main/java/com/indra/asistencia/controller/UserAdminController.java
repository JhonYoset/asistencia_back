package com.indra.asistencia.controller;

import com.indra.asistencia.dto.CreateUserRequestDto;
import com.indra.asistencia.dto.UserResponseDto;
import com.indra.asistencia.service.IUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final IUserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listarUsuarios() {
        List<UserResponseDto> usuarios = userAdminService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<String> crearUsuario(@RequestBody CreateUserRequestDto dto) {
        String resultado = userAdminService.crearUsuario(dto);
        return ResponseEntity.ok(resultado);
    }
}
package com.indra.asistencia.controller;

import com.indra.asistencia.dto.CreateUserRequestDto;
import com.indra.asistencia.service.IUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final IUserAdminService userAdminService;

    @PostMapping
    public ResponseEntity<String> crearUsuario(@RequestBody CreateUserRequestDto dto) {
        return ResponseEntity.ok(userAdminService.crearUsuario(dto));
    }
}
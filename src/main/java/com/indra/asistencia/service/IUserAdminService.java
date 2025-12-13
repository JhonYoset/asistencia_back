package com.indra.asistencia.service;

import com.indra.asistencia.dto.CreateUserRequestDto;
import com.indra.asistencia.dto.UserResponseDto;

import java.util.List;

public interface IUserAdminService {
    String crearUsuario(CreateUserRequestDto dto);
    List<UserResponseDto> listarUsuarios();
    UserResponseDto obtenerUsuarioPorId(Long id);
    String actualizarUsuario(Long id, CreateUserRequestDto dto);
    String desactivarUsuario(Long id);
    String activarUsuario(Long id);
}
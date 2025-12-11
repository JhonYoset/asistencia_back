package com.indra.asistencia.service;

import com.indra.asistencia.dto.CreateUserRequestDto;

public interface IUserAdminService {
    String crearUsuario(CreateUserRequestDto dto);
}
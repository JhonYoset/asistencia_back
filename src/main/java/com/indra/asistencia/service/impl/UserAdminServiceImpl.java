package com.indra.asistencia.service.impl;


import com.indra.asistencia.dto.CreateUserRequestDto;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.models.Role;
import com.indra.asistencia.models.User;
import com.indra.asistencia.models.UserRols;
import com.indra.asistencia.repository.IRoleRepository;
import com.indra.asistencia.repository.IUserRepository;
import com.indra.asistencia.repository.IUserRoleRepository;
import com.indra.asistencia.service.IUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements IUserAdminService {

    private final IUserRepository userRepo;
    private final IRoleRepository roleRepo;
    private final IUserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String crearUsuario(CreateUserRequestDto dto) {
        // Validar que el username no exista
        if (userRepo.getByUserName(dto.getUsername()).isPresent()) {
            throw new ValidatedRequestException("El usuario ya existe");
        }

        // Crear usuario
        User nuevo = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(true)
                .build();
        userRepo.save(nuevo);

        // Buscar el rol
        Role rol = roleRepo.findByName(dto.getRol().toUpperCase())
                .orElseThrow(() -> new ValidatedRequestException("Rol no válido: " + dto.getRol()));

        // Asignar rol
        UserRols userRol = UserRols.builder()
                .user(nuevo)
                .role(rol)
                .build();
        userRoleRepo.save(userRol);

        return "Usuario " + dto.getUsername() + " creado con rol " + dto.getRol();
    }
}

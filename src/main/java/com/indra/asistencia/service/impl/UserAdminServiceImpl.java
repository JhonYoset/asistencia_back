package com.indra.asistencia.service.impl;

import com.indra.asistencia.dto.CreateUserRequestDto;
import com.indra.asistencia.dto.UserResponseDto;
import com.indra.asistencia.exception.ResourceNotFoundException;
import com.indra.asistencia.exception.ValidatedRequestException;
import com.indra.asistencia.mappers.UserMapper;
import com.indra.asistencia.models.*;
import com.indra.asistencia.repository.*;
import com.indra.asistencia.service.IUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements IUserAdminService {

    private final IUserRepository userRepo;
    private final IRoleRepository roleRepo;
    private final IUserRoleRepository userRoleRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final UserMapper userMapper;
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

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> listarUsuarios() {
        List<User> usuarios = userRepo.findAll();
        
        return usuarios.stream()
                .map(usuario -> {
                    // Obtener roles del usuario
                    List<UserRols> roles = userRoleRepo.getRolesByUser(usuario.getId());
                    String rol = roles != null && !roles.isEmpty() 
                        ? roles.get(0).getRole().getName() 
                        : "EMPLEADO";
                    
                    // Obtener asistencias del usuario
                    List<Asistencia> asistencias = asistenciaRepo.findAll().stream()
                            .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                            .collect(Collectors.toList());
                    
                    return userMapper.toDto(usuario, rol, asistencias.size());
                })
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto obtenerUsuarioPorId(Long id) {
        User usuario = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        
        List<UserRols> roles = userRoleRepo.getRolesByUser(usuario.getId());
        String rol = roles != null && !roles.isEmpty() 
            ? roles.get(0).getRole().getName() 
            : "EMPLEADO";
        
        List<Asistencia> asistencias = asistenciaRepo.findAll().stream()
                .filter(a -> a.getUsuario().getId().equals(usuario.getId()))
                .collect(Collectors.toList());
        
        return userMapper.toDto(usuario, rol, asistencias.size());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarUsuario(Long id, CreateUserRequestDto dto) {
        User usuario = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Verificar si el nuevo username ya existe (si cambió)
        if (!usuario.getUsername().equals(dto.getUsername())) {
            if (userRepo.getByUserName(dto.getUsername()).isPresent()) {
                throw new ValidatedRequestException("El nombre de usuario ya está en uso");
            }
            usuario.setUsername(dto.getUsername());
        }
        
        // Actualizar contraseña si se proporcionó
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        usuario.setEnabled(true); // Siempre activo al actualizar
        
        userRepo.save(usuario);
        
        // Actualizar rol si es necesario
        List<UserRols> roles = userRoleRepo.getRolesByUser(usuario.getId());
        if (!roles.isEmpty()) {
            Role nuevoRol = roleRepo.findByName(dto.getRol().toUpperCase())
                    .orElseThrow(() -> new ValidatedRequestException("Rol no válido: " + dto.getRol()));
            
            UserRols userRol = roles.get(0);
            userRol.setRole(nuevoRol);
            userRoleRepo.save(userRol);
        }
        
        return "Usuario actualizado correctamente";
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String desactivarUsuario(Long id) {
        User usuario = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        usuario.setEnabled(false);
        userRepo.save(usuario);
        
        return "Usuario desactivado correctamente";
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String activarUsuario(Long id) {
        User usuario = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        usuario.setEnabled(true);
        userRepo.save(usuario);
        
        return "Usuario activado correctamente";
    }
}
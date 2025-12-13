package com.indra.asistencia.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.indra.asistencia.models.User;
import com.indra.asistencia.repository.IUserRepository;
import com.indra.asistencia.repository.IUserRoleRepository;

@Service
public class UserServiceImpl implements UserDetailsService {
    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;

    public UserServiceImpl(IUserRepository userRepository, 
                          IUserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Buscando usuario: " + username);
        
        User user = userRepository.getByUserName(username)
            .orElseThrow(() -> {
                System.out.println("Usuario NO encontrado: " + username);
                return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });
        
        System.out.println("Usuario encontrado: " + username + " (ID: " + user.getId() + ")");
        
        var userRols = userRoleRepository.getRolesByUser(user.getId());
        
        System.out.println("Roles encontrados para " + username + ": " + 
            userRols.stream()
                .map(ur -> ur.getRole().getName())
                .toList());
        
        if (userRols.isEmpty()) {
            System.out.println("ADVERTENCIA: Usuario " + username + " no tiene roles asignados");
        }
        
        return new UserDetailsImpl(user, userRols);
    }
}
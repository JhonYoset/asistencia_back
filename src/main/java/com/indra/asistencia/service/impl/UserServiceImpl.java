package com.indra.asistencia.service.impl;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.indra.asistencia.models.User;
import com.indra.asistencia.repository.IUserRepository;
import com.indra.asistencia.repository.IUserRoleRepository;



@Service
public class UserServiceImpl implements  UserDetailsService {


    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;

    public UserServiceImpl(IUserRepository userRepository, IUserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.getByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        var userRols = userRoleRepository.getRolesByUser(user.getId());

        return new UserDetailsImpl(user, userRols);

    }



}

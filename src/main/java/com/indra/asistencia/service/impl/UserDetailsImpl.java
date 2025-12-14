package com.indra.asistencia.service.impl;

import java.util.Collection;
import java.util.List;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import com.indra.asistencia.models.User;
import com.indra.asistencia.models.UserRols;





public class UserDetailsImpl implements UserDetails {



    private  User user;
    private List<UserRols> userRols;


    public UserDetailsImpl(User user, List<UserRols> userRols) {
         this.user = user;
         this.userRols = userRols;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {


        if(userRols.size() == 0){
            return List.of();
        }

        var authorities = userRols.stream()
            .map(userRol ->  new SimpleGrantedAuthority(  "ROLE_" + userRol.getRole().getName() )  )
            .toList();

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;

    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}

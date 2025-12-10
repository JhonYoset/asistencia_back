package com.indra.asistencia.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import com.indra.asistencia.Jwt.JwtUtil;
import com.indra.asistencia.exception.ExceptionDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws IOException, ServletException  {
        try {
            String authHeader = request.getHeader("Authorization");
            String username = null;
            String jwt = null;
            List<String> roles = new ArrayList<>();

            if(authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtUtil.getUsername(jwt);
                roles = jwtUtil.getRoles(jwt); 
            }

            if(username != null  && SecurityContextHolder.getContext().getAuthentication() == null) {
                if(jwtUtil.validateToken(jwt, username)) {

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            if(role.contains("ROLE_")){
                                role = role.replace("ROLE_","");
                            }
                            return new SimpleGrantedAuthority("ROLE_" + role);

                        })
                        .toList();

                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                    WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);
                    authentication.setDetails(details);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException ex) {

            ExceptionDto dto = ExceptionDto.builder()
                .hora(LocalDateTime.now().toString())
                .codeStatus(401)
                //.mensaje(ex.getMessage())
                .mensaje("Token invalido o expirado")
                .url(request.getRequestURI())
                .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            // response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() +
            //         "\",\"status\":401,\"error\":\"JWT Error\",\"message\":\"" + ex.getMessage() +
            //         "\",\"path\":\"" + request.getRequestURI() + "\"}");
            response.getWriter().write(new ObjectMapper().writeValueAsString(dto));
        }
    }


}

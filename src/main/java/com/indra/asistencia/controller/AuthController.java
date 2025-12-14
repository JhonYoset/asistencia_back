package com.indra.asistencia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.indra.asistencia.Jwt.JwtUtil;
import com.indra.asistencia.exception.ValidatedRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
 
    @Autowired
    private AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        logger.info("=== INICIO LOGIN ===");
        logger.info("Username recibido: {}", username);
        
        try {
            Authentication auth = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            logger.info("✅ Usuario autenticado: {}", username);

            List<String> roles = auth.getAuthorities().stream()
                .map(r -> r.getAuthority())
                .toList();
            
            logger.info("Roles del usuario: {}", roles);

            String token = jwtUtil.generateToken(username, roles);
            
            logger.info("✅ Token generado (primeros 30 caracteres): {}", 
                       token.substring(0, Math.min(30, token.length())));
            logger.info("=== FIN LOGIN EXITOSO ===");
            
            return token;
            
        } catch (Exception e) {
            logger.error("❌ Error en login para usuario: {}", username);
            logger.error("Detalle del error:", e);
            throw new ValidatedRequestException("Credenciales inválidas");
        }
    }
}
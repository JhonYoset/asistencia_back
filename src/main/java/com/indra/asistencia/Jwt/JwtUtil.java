package com.indra.asistencia.Jwt;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;


@Component
public class JwtUtil {


    private final String SECRET = "mysecretkey001";

    public String generateToken(String username) {

        return JWT.create()
            .withSubject(username)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) 
            .sign(Algorithm.HMAC256(SECRET));
    }

    public String generateToken(String username, List<String> roles) {
        
        return JWT.create()
            .withSubject(username)
            .withClaim("roles",roles)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) 
            .sign(Algorithm.HMAC256(SECRET));
    }

    public boolean validateToken(String token, String username) {

        DecodedJWT jwt = getDecodedJWT(token);
        return  jwt.getSubject().equals(username) && jwt.getExpiresAt().after(new Date());

    }
    public String getUsername(String token) {
        return getDecodedJWT(token).getSubject();
    }    

    private DecodedJWT getDecodedJWT(String token) {

        
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        return verifier.verify(token);
            

    }    

    public List<String> getRoles(String token) {
        DecodedJWT jwt = getDecodedJWT(token);
        return jwt.getClaim("roles").asList(String.class);
    }

}

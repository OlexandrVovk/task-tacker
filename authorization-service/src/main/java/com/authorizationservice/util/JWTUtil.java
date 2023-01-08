package com.authorizationservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JWTUtil {
    private final UserDetailsService userDetailsService;

    @Value("${jwt_secret}")
    private String secret;


    public String generateToken(Long id){
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());
        return JWT.create()
                .withSubject("User details")
                .withClaim("id", id)
                .withIssuedAt(new Date())
                .withIssuer("authorization-service")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveClaim(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("authorization-service")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("username").asString();
    }

    public UserDetails parseRequest(HttpServletRequest request){
        UserDetails userDetails = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader!=null && authHeader.isBlank() && authHeader.startsWith("Bearer ")){
            String jwt = authHeader.substring(7);
            if (jwt.isBlank()){
                //TODO: throw exception missing Jwt token
            }else {
                try {
                    String username = validateTokenAndRetrieveClaim(jwt);
                    userDetails = userDetailsService.loadUserByUsername(username);
                }catch (JWTVerificationException e){
                    //TODO: throw exception invalid JWT token
                }
            }
        }else {
            //todo: throw bad request exception
        }
        return userDetails;
    }
}

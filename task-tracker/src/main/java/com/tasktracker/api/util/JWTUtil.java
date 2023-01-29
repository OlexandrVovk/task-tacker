package com.tasktracker.api.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;


    public Long validateTokenAndRetrieveClaim(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("authorization-service")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("id").asLong();
    }

    public Long getPersonId(HttpServletRequest request){
        String token = request.getHeader("Authorization").substring(7);
        Long personId = this.validateTokenAndRetrieveClaim(token);
        return personId;
    }

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

}

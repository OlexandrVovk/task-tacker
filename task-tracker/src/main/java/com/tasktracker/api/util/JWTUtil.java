package com.tasktracker.api.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;


    public int validateTokenAndRetrieveClaim(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("authorization-service")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("id").asInt();
    }

    public Long getPersonId(HttpServletRequest request){
        String token = request.getHeader("Authorization").substring(7);
        int personId = this.validateTokenAndRetrieveClaim(token);
        return Long.valueOf(personId);
    }

}

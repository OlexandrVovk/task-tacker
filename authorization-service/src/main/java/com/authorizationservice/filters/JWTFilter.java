package com.authorizationservice.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.authorizationservice.services.CustomUserDetailsService;
import com.authorizationservice.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("``````````````");
        String authHeader = request.getHeader("Authorization");
       if (authHeader!=null && authHeader.isBlank() && authHeader.startsWith("Bearer ")){
           String jwt = authHeader.substring(7);

           if (jwt.isBlank()){
               response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                       "Missing JWT token");
           }else {
               try {
                   String username = jwtUtil.validateTokenAndRetrieveClaim(jwt);
                   UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                   UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                           userDetails.getPassword(),
                           userDetails.getAuthorities());

                   if (SecurityContextHolder.getContext().getAuthentication() == null) {
                       SecurityContextHolder.getContext().setAuthentication(authToken);
                   }
               }catch (JWTVerificationException e){
                   response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Invalid JWT token");
               }
           }
       }
       filterChain.doFilter(request, response);
    }
}

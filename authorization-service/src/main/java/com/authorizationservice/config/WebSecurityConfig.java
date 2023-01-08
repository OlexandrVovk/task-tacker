package com.authorizationservice.config;

import com.authorizationservice.filters.JWTFilter;
import com.authorizationservice.filters.JWTAuthenticationFilter;
import com.authorizationservice.services.CustomUserDetailsService;
import com.authorizationservice.util.CustomAuthenticationManager;
import com.authorizationservice.util.JWTUtil;
import jakarta.servlet.Filter;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final JWTUtil jwtUtil;



    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authenticationProvider(authenticationProvider())
                .addFilter(tokenProcessingFilter());

        http.addFilterAfter(jwtFilter(), JWTAuthenticationFilter.class);

        http.authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest()
                .authenticated()
                .and().httpBasic();

        return http.build();
    }


    @Bean
    public JWTAuthenticationFilter tokenProcessingFilter(){
        JWTAuthenticationFilter filter = new JWTAuthenticationFilter(jwtUtil);
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Bean
    public JWTFilter jwtFilter(){
        return new JWTFilter(jwtUtil, userDetailsService);
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        return customAuthenticationManager;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }


}

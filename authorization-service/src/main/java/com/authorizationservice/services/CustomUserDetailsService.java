package com.authorizationservice.services;

import com.authorizationservice.repositories.PersonRepo;
import com.authorizationservice.security.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonRepo personRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomUserDetails(personRepo.findByName(username).orElseThrow(() -> {
            throw new UsernameNotFoundException("User was not found");
        }));
    }
}

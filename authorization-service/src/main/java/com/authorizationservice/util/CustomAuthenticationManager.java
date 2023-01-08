package com.authorizationservice.util;

import com.authorizationservice.entity.Person;
import com.authorizationservice.repositories.PersonRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;


@AllArgsConstructor
@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    private final PersonRepo personRepo;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
       Optional<Person> foundPerson = personRepo.findByName(authentication.getName());
       if (foundPerson.isPresent()){
          if (foundPerson.get().getPassword().equals(authentication.getCredentials().toString())){
              return new UsernamePasswordAuthenticationToken(
                      foundPerson.get().getName(),
                      foundPerson.get().getPassword());
          }else {
              //todo: throw invalid jwt token exception
          }
       }else {
           //todo: trow bad request excepton
       }
       return null;
    }
}

package com.authorizationservice.controllers;

import com.authorizationservice.dto.PersonDto;
import com.authorizationservice.entity.Person;
import com.authorizationservice.services.AuthorizationService;
import com.authorizationservice.util.JWTUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class AuthorizationController {

    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final AuthorizationService authorizationService;

    private static final String PERFORM_REGISTRATION = "/api/auth/registration";
    private static final String PERFORM_LOGIN = "/api/auth/login";

    @PostMapping(PERFORM_REGISTRATION)
    public Map<String, String> performRegistration(@RequestBody PersonDto personDto){
        Person person = convertToUser(personDto);
        Person authorizedPerson = authorizationService.register(person);


        String token = jwtUtil.generateToken(authorizedPerson.getId());
        return Map.of("jwt-token",token);
    }

    @PostMapping(PERFORM_LOGIN)
    public Map<String, String> performLogin(@RequestBody PersonDto personDto){
        Person person = convertToUser(personDto);

        Person authorizedPerson = authorizationService.login(person);
        String token = jwtUtil.generateToken(authorizedPerson.getId());
        return Map.of("jwt-token",token);
    }

    private Person convertToUser(PersonDto personDto){
        return this.modelMapper.map(personDto, Person.class);
    }
}

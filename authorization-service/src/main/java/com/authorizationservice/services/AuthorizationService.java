package com.authorizationservice.services;

import com.authorizationservice.entity.Person;
import com.authorizationservice.repositories.PersonRepo;
import jakarta.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorizationService {

    private final PersonRepo personRepo;

    public Person register(Person person) {
        Optional<Person> foundPerson = personRepo.findByName(person.getName());
        foundPerson.ifPresent(p -> {
            throw new BadRequestException(String.format("User %s is already created", person.getName()));
        });

        return personRepo.saveAndFlush(person);
    }

    public Person login(Person person){
        Optional<Person> foundPerson = personRepo.findByName(person.getName());
        if (foundPerson.isEmpty()){
            throw new BadRequestException(String.format("User %s was not found", person.getName()));
        }
        if  (!person.getPassword().equals(foundPerson.get().getPassword())){
            System.out.println(person.getPassword() + " " + foundPerson.get().getPassword());
            throw new BadRequestException("Incorrect password");
        }
        return foundPerson.get();
    }
}

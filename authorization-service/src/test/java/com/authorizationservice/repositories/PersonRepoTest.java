package com.authorizationservice.repositories;

import com.authorizationservice.entity.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PersonRepoTest {

    private final PersonRepo personRepo;

    @Autowired
    PersonRepoTest(PersonRepo personRepo) {
        this.personRepo = personRepo;
    }

    @Test
    void findByName() {
        Person person = personRepo.saveAndFlush(
                Person.builder()
                .name("test user")
                .password("test password")
                .build());

        Optional<Person> resultPerson = personRepo.findByName("test user");

        assertTrue(resultPerson.isPresent());
        assertTrue(resultPerson.get().getPassword().equals(person.getPassword()));
    }
}
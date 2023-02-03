package com.authorizationservice.services;

import com.authorizationservice.entity.Person;
import com.authorizationservice.exceptions.BadRequestException;
import com.authorizationservice.repositories.PersonRepo;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = AuthorizationServiceTest.class)
class AuthorizationServiceTest {

    @Mock
    private PersonRepo personRepo;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldRegisterUser() {
        Person person = mock(Person.class);
        when(personRepo.findByName(anyString())).thenReturn(Optional.empty());
        when(person.getName()).thenReturn("bla bla name");

        authorizationService.register(person);

        verify(personRepo).saveAndFlush(any(Person.class));
    }

    @Test
    void shouldThrowBadRequestExcWhenSameUserExists_RegisterUser() {
        Person person = mock(Person.class);
        Person foundPerson = mock(Person.class);
        when(personRepo.findByName(anyString())).thenReturn(Optional.ofNullable(foundPerson));
        when(person.getName())
                .thenReturn("bla bla name")
                .thenReturn("bla bla name");

        assertThrows(BadRequestException.class, () -> {authorizationService.register(person);});

    }


    @Test
    void shouldLoginUser() {
        Person foundPerson = spy(Person.builder()
                .id(1l)
                .name("test")
                .password("test")
                .build());
        Person person = spy(Person.builder()
                .id(1l)
                .name("test")
                .password("test")
                .build());
        when(personRepo.findByName(anyString())).thenReturn(Optional.ofNullable(foundPerson));

        Person resultPerson = authorizationService.login(person);

        assertEquals(foundPerson, resultPerson);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenIncorrectPassword_LoginUser() {
        Person foundPerson = spy(Person.builder()
                .id(1l)
                .name("test")
                .password("test 1")
                .build());
        Person person = spy(Person.builder()
                .id(1l)
                .name("test")
                .password("test 2")
                .build());
        when(personRepo.findByName(anyString())).thenReturn(Optional.ofNullable(foundPerson));

        assertThrows(BadRequestException.class, () -> {authorizationService.login(person);});
    }
}
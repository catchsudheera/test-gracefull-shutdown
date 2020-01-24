package com.sudheera.playground.spring.testgracefullshutdown.service;

import com.sudheera.playground.spring.testgracefullshutdown.model.Person;
import com.sudheera.playground.spring.testgracefullshutdown.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void savePerson(String name) {
        Person person = new Person(name);
        personRepository.save(person);
    }

    public Optional<Person> loadPerson(String name) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        return personRepository.findFirstByName(name);
    }
}

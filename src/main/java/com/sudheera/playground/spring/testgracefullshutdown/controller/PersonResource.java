package com.sudheera.playground.spring.testgracefullshutdown.controller;

import com.sudheera.playground.spring.testgracefullshutdown.model.Person;
import com.sudheera.playground.spring.testgracefullshutdown.service.PersonService;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@BasePathAwareController
public class PersonResource {

    private final PersonService personService;

    public PersonResource(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping(path = "add")
    public void addPerson(@RequestParam(name = "name") String name) {
        personService.savePerson(name);
    }

    @GetMapping(path = "load")
    public Person loadPerson(@RequestParam(name = "name") String name) {
        return personService.loadPerson(name).orElse(null);
    }
}
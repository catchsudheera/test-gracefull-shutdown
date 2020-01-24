package com.sudheera.playground.spring.testgracefullshutdown.repository;

import com.sudheera.playground.spring.testgracefullshutdown.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findFirstByName(String name);

}

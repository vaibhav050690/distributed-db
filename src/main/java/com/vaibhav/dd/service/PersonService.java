package com.vaibhav.dd.service;

import com.vaibhav.dd.dto.Person;
import java.util.List;

public interface PersonService {

    Person getById(Long id);

    void addPerson(Person person);

    List<Person> getAll();

    void refresh(List<Person> personList);

}

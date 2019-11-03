package com.vaibhav.dd.service;

import com.vaibhav.dd.dto.Person;
import com.vaibhav.dd.storage.PersonStorage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "personService")
@Slf4j
public class PersonServiceImpl implements PersonService {

    @Autowired
    private PersonStorage personStorage;


    @Override
    public Person getById(Long id) {
        return personStorage.get(id);
    }

    @Override
    public void addPerson(Person person) {
        personStorage.put(person.getId(), person);
    }

    @Override
    public List<Person> getAll() {
        return personStorage.findAll();
    }

    @Override
    public void refresh(List<Person> personList) {
        personStorage.clear();
        log.info("personStorage : {}", personStorage.findAll());
        for(Person person : personList){
            log.info("person : {}", person);
            personStorage.put(person.getId(), person);
        }
    }

}

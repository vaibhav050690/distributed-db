package com.vaibhav.dd.storage;

import com.vaibhav.dd.dto.Person;
import java.util.List;

public interface PersonStorage {

    Person get(Long key);

    void put(Long key, Person value);

    List<Person> findAll();

    void clear();

}

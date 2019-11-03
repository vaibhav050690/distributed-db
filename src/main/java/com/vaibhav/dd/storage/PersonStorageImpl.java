package com.vaibhav.dd.storage;

import com.vaibhav.dd.dto.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PersonStorageImpl implements PersonStorage {

    private ConcurrentHashMap<Long, Person> personMap = new ConcurrentHashMap<>();

    @Override
    public Person get(Long key) {
        return personMap.get(key);
    }

    @Override
    public void put(Long key, Person value) {
        personMap.put(key, value);
    }

    @Override
    public List<Person> findAll() {
        List<Person> result = new ArrayList<>();
        for (Map.Entry<Long, Person> entry : personMap.entrySet()) {
            Person person = entry.getValue();
            result.add(new Person(person.getId(), person.getName()));
        }
        return result;
    }

    @Override
    public void clear() {
        personMap.clear();
    }
}

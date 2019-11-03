package com.vaibhav.dd.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vaibhav.dd.dto.Person;
import com.vaibhav.dd.storage.ClusterInfo;
import com.vaibhav.dd.utils.DistributedDatabaseHelper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncServiceImpl implements SyncService {

    @Autowired
    private DistributedDatabaseHelper distributedDatabaseHelper;

    @Autowired
    private PersonService personService;

    @Override
    public ResponseEntity<Void> syncData(String hostPortString, Person person, boolean isLeader) {
        return restTemplate
            .exchange(distributedDatabaseHelper.getAddPersonEndpoint(hostPortString),
                HttpMethod.PUT,
                distributedDatabaseHelper.getAddPersonRequest(person, isLeader), Void.class);

    }

    @Override
    public void syncDataFromMaster() {
        if (!ClusterInfo.getClusterInfo().getMasterNode().isEmpty() && !distributedDatabaseHelper
            .isLeader(distributedDatabaseHelper.getServerString())) {
            JsonNode jsonNode = restTemplate
                .getForObject(distributedDatabaseHelper.getAddPersonEndpoint(
                    ClusterInfo.getClusterInfo().getMasterNode()), JsonNode.class);
            List<Person> personList = new ObjectMapper().convertValue(jsonNode,
                TypeFactory.defaultInstance().constructCollectionType(List.class, Person.class));
            personService.refresh(personList);
        }
    }

}

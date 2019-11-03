package com.vaibhav.dd.controller;

import com.google.gson.Gson;
import com.vaibhav.dd.dto.Person;
import com.vaibhav.dd.service.PersonService;
import com.vaibhav.dd.service.SyncService;
import com.vaibhav.dd.storage.ClusterInfo;
import com.vaibhav.dd.utils.DistributedDatabaseHelper;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/db/")
public class DistributedDatabaseController {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private PersonService personService;

    @Autowired
    private DistributedDatabaseHelper distributedDatabaseHelper;

    @Autowired
    private SyncService syncService;

    private Gson gson = new Gson();

    @RequestMapping(
        value = {"person/{id}"},
        method = {RequestMethod.GET}
    )
    public ResponseEntity<Person> getPerson(@PathVariable Long id) {
        Person person = personService.getById(id);
        if (person != null) {
            return new ResponseEntity<>(person, HttpStatus.OK);
        }
        return new ResponseEntity<>(person, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(
        value = {"person/"},
        method = {RequestMethod.GET}
    )
    public ResponseEntity<List<Person>> getAllPerson() {
        return new ResponseEntity<List<Person>>(personService.getAll(), HttpStatus.OK);
    }


    @RequestMapping(
        value = {"person/"},
        method = {RequestMethod.PUT}
    )
    public ResponseEntity<Void> addPerson(@RequestBody Person person,
        @RequestHeader Map<String, String> headers) {
        if (isRequestFromLeader(headers)) {
            log.info("received update request from leader, applying changes {}", person);
            personService.addPerson(person);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }
        if (distributedDatabaseHelper.isLeader(distributedDatabaseHelper.getServerString())) {
            List<String> liveNodes = ClusterInfo.getClusterInfo().getLiveNodes();
            for (String node : liveNodes) {
                if (!distributedDatabaseHelper.isLeader(node)) {
                    log.info("sending add person request to follower {}", node);
                    syncService.syncData(node, person, true);
                }
            }
            personService.addPerson(person);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        } else {
            //forward request to leader
            log.info("forwarding add person request to leader {}",
                ClusterInfo.getClusterInfo().getMasterNode());
            return syncService
                .syncData(ClusterInfo.getClusterInfo().getMasterNode(), person, false);
        }
    }

    private boolean isRequestFromLeader(Map<String, String> headers) {
        if (headers.containsKey("request-from")) {
            return ClusterInfo.getClusterInfo().getMasterNode().equals(headers.get("request-from"));
        }
        return false;
    }


    @RequestMapping(
        value = {"cluster-info/"},
        method = {RequestMethod.GET}
    )
    public ResponseEntity<ClusterInfo> getClusterInfo() {
        return new ResponseEntity<>(ClusterInfo.getClusterInfo(), HttpStatus.NOT_FOUND);
    }


}

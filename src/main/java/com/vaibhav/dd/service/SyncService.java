package com.vaibhav.dd.service;

import com.google.gson.Gson;
import com.vaibhav.dd.dto.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public interface SyncService {

    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<Void> syncData(String hostPortString, Person person, boolean isLeader);

    void syncDataFromMaster();

}

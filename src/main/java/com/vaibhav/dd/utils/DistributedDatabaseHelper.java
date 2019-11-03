package com.vaibhav.dd.utils;

import com.google.gson.Gson;
import com.vaibhav.dd.dto.Person;
import com.vaibhav.dd.storage.ClusterInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public final class DistributedDatabaseHelper {

    @Value("${node.name}")
    private String nodeName;

    @Value("${server.port}")
    private String port;

    private Gson gson = new Gson();

    public String getServerString() {
        return nodeName.concat(":").concat(port);
    }

    public boolean isLeader(String node) {
        return ClusterInfo.getClusterInfo().getMasterNode().equals(node);
    }

    public HttpEntity<String> getAddPersonRequest(Person person, boolean fromLeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (fromLeader) {
            headers.add("request-from", ClusterInfo.getClusterInfo().getMasterNode());
        }
        return new HttpEntity<>(gson.toJson(person), headers);
    }

    public String getAddPersonEndpoint(String hostPortString) {
        StringBuilder endpointBuilder = new StringBuilder("http://");
        endpointBuilder.append(hostPortString);
        endpointBuilder.append("/db/person/");
        return endpointBuilder.toString();
    }

    public String getAllPersonEndpoint(String hostPortString) {
        StringBuilder endpointBuilder = new StringBuilder("http://");
        endpointBuilder.append(hostPortString);
        endpointBuilder.append("/db/person/");
        return endpointBuilder.toString();
    }



}

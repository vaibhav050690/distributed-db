package com.vaibhav.dd.service;

import com.vaibhav.dd.serializer.StringSerializer;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ZookeeperServiceImpl implements ZookeeperService {


    private ZkClient zkClient;

    public ZookeeperServiceImpl(@Value("${zookeeper.server.string}") String serverString) {
        zkClient = new ZkClient(serverString, 12000, 5000, new StringSerializer());
    }

    @Override
    public void createRootNodes() {
        if (!zkClient.exists(ALL_NODES)) {
            log.info("creating node {} ", ALL_NODES);
            createNode(ALL_NODES, "Data for all nodes in db cluster.", CreateMode.PERSISTENT);
        }
        if (!zkClient.exists(LIVE_NODES)) {
            log.info("creating node {} ", LIVE_NODES);
            createNode(LIVE_NODES, "Data for all live nodes in db cluster.", CreateMode.PERSISTENT);
        }
        if (!zkClient.exists(ELECTION)) {
            log.info("creating node {} ", ELECTION);
            createNode(ELECTION, "Leader election node for db cluster.", CreateMode.PERSISTENT);
        }
    }

    private void createNode(String name, String data, CreateMode mode) {
        zkClient.create(name, data, mode);
    }

    @Override
    public void addToAllNodes(String node) {
        if (!zkClient.exists(ALL_NODES)) {
            log.error("{} not yet created.", ALL_NODES);
            throw new RuntimeException(ALL_NODES + " not yet created");
        }
        String path = ALL_NODES.concat("/".concat(node));
        if (!zkClient.exists(path)) {
            log.info("adding node {} ", node);
            createNode(path, "cluster node", CreateMode.PERSISTENT);
        }
    }

    @Override
    public List<String> getAllNodes() {
        if (!zkClient.exists(ALL_NODES)) {
            throw new RuntimeException("No node /allNodes exists");
        }
        return zkClient.getChildren(ALL_NODES);
    }

    @Override
    public void addToLiveNodes(String node) {
        if (!zkClient.exists(LIVE_NODES)) {
            log.error("{} not yet created.", LIVE_NODES);
            throw new RuntimeException(LIVE_NODES + " not yet created");
        }
        String path = LIVE_NODES.concat("/".concat(node));
        if (!zkClient.exists(path)) {
            createNode(path, "cluster live node", CreateMode.EPHEMERAL);
        }
    }

    @Override
    public List<String> getLiveNodes() {
        if (!zkClient.exists(LIVE_NODES)) {
            throw new RuntimeException("No node /allNodes exists");
        }
        return zkClient.getChildren(LIVE_NODES);
    }

    @Override
    public void registerChildChangeWatcher(String path, IZkChildListener watcher) {
        zkClient.subscribeChildChanges(path, watcher);
    }

    @Override
    public void registerStateChangeWatcher(IZkStateListener watcher) {
        zkClient.subscribeStateChanges(watcher);
    }

    @Override
    public String getLeaderNode() {
        String leader = "";
        if (zkClient.exists(ELECTION)) {
            List<String> children = zkClient.getChildren(ELECTION);
            if (children != null && !children.isEmpty()) {
                Collections.sort(children);
                leader = getZNodeData(ELECTION.concat("/").concat(children.get(0)));
            }
        }
        return leader;
    }

    @Override
    public void addToElectionNode(String node) {
        if (!zkClient.exists(ELECTION)) {
            log.error("{} not yet created.", ELECTION);
            throw new RuntimeException(ELECTION + " not yet created");
        }
        createNode(ELECTION.concat("/LEADER_"), node, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @Override
    public String getZNodeData(String path) {
        return zkClient.readData(path);
    }

}

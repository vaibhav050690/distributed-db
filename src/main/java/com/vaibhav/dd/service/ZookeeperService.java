package com.vaibhav.dd.service;

import java.util.List;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;

public interface ZookeeperService {

    String ALL_NODES = "/all_nodes";

    String LIVE_NODES = "/live_nodes";

    String ELECTION = "/election";

    void createRootNodes();

    void addToAllNodes(String node);

    List<String> getAllNodes();

    void addToLiveNodes(String node);

    List<String> getLiveNodes();

    void registerChildChangeWatcher(String path, IZkChildListener watcher);

    void registerStateChangeWatcher(IZkStateListener watcher);

    String getLeaderNode();

    void addToElectionNode(String node);

    String getZNodeData(String path);

}

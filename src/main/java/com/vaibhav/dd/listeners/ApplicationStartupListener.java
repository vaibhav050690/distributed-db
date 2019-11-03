package com.vaibhav.dd.listeners;

import com.vaibhav.dd.listeners.watchers.AllNodeChangeListener;
import com.vaibhav.dd.listeners.watchers.ElectionNodeChangeListener;
import com.vaibhav.dd.listeners.watchers.LiveNodeChangeListener;
import com.vaibhav.dd.listeners.watchers.StateChangeListener;
import com.vaibhav.dd.service.SyncService;
import com.vaibhav.dd.service.ZookeeperService;
import com.vaibhav.dd.storage.ClusterInfo;
import com.vaibhav.dd.utils.DistributedDatabaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ZookeeperService zookeeperService;

    @Autowired
    private AllNodeChangeListener allNodeChangeListener;

    @Autowired
    private LiveNodeChangeListener liveNodeChangeListener;

    @Autowired
    private ElectionNodeChangeListener electionNodeChangeListener;

    @Autowired
    private StateChangeListener stateChangeListener;

    @Autowired
    private DistributedDatabaseHelper distributedDatabaseHelper;

    @Autowired
    private SyncService syncService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        zookeeperService.createRootNodes();
        String serverString = distributedDatabaseHelper.getServerString();

        log.info("registering {} under {} in Zookeeper", serverString, ZookeeperService.ALL_NODES);
        zookeeperService.addToAllNodes(serverString);
        ClusterInfo.getClusterInfo().getAllNodes().clear();
        ClusterInfo.getClusterInfo().getAllNodes().addAll(zookeeperService.getAllNodes());

        log.info("registering {} under {} in Zookeeper", serverString, ZookeeperService.ELECTION);
        zookeeperService.addToElectionNode(serverString);
        String leader = zookeeperService.getLeaderNode();
        ClusterInfo.getClusterInfo().setMasterNode(leader);

        log.info("syncing data from master");
        syncService.syncDataFromMaster();

        log.info("registering {} under {} in Zookeeper", serverString, ZookeeperService.LIVE_NODES);
        zookeeperService.addToLiveNodes(serverString);
        ClusterInfo.getClusterInfo().getLiveNodes().clear();
        ClusterInfo.getClusterInfo().getLiveNodes().addAll(zookeeperService.getLiveNodes());

        //register watchers
        zookeeperService
            .registerChildChangeWatcher(ZookeeperService.ELECTION, electionNodeChangeListener);
        zookeeperService
            .registerChildChangeWatcher(ZookeeperService.ALL_NODES, allNodeChangeListener);
        zookeeperService
            .registerChildChangeWatcher(ZookeeperService.LIVE_NODES, liveNodeChangeListener);
        zookeeperService.registerStateChangeWatcher(stateChangeListener);
    }

}

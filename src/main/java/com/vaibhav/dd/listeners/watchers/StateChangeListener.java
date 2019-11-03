package com.vaibhav.dd.listeners.watchers;

import com.vaibhav.dd.service.SyncService;
import com.vaibhav.dd.service.ZookeeperService;
import com.vaibhav.dd.storage.ClusterInfo;
import com.vaibhav.dd.utils.DistributedDatabaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
    public class StateChangeListener implements IZkStateListener {

    @Autowired
    private DistributedDatabaseHelper distributedDatabaseHelper;

    @Autowired
    private SyncService syncService;

    @Autowired
    private ZookeeperService zookeeperService;

    @Override
    public void handleStateChanged(KeeperState keeperState) throws Exception {
        log.info("state changed for {}, state : {}", distributedDatabaseHelper.getServerString(), keeperState.name());
    }

    @Override
    public void handleNewSession() throws Exception {
        String serverString = distributedDatabaseHelper.getServerString();
        log.info("{} established session to zookeeper", serverString);

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
    }

    @Override
    public void handleSessionEstablishmentError(Throwable throwable) throws Exception {
        log.warn("{} could not establish session to zookeeper", distributedDatabaseHelper.getServerString());
    }
}

package com.vaibhav.dd.listeners.watchers;

import com.vaibhav.dd.service.ZookeeperService;
import com.vaibhav.dd.storage.ClusterInfo;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ElectionNodeChangeListener implements IZkChildListener {

    @Autowired
    private ZookeeperService zookeeperService;

    @Override
    public void handleChildChange(String path, List<String> childrens) throws Exception {
        log.info("Child Change event at {}", path);
        log.info("childrens {}", childrens);
        if(childrens.isEmpty()){
            log.warn("No nodes under {} to elect leader.", ZookeeperService.ELECTION);
            return;
        }
        else {
            Collections.sort(childrens);
            String leaderPath = ZookeeperService.ELECTION.concat("/").concat(childrens.get(0));
            String leader = zookeeperService.getZNodeData(leaderPath);
            ClusterInfo.getClusterInfo().setMasterNode(leader);
        }
    }
}

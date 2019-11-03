package com.vaibhav.dd.listeners.watchers;

import com.vaibhav.dd.storage.ClusterInfo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AllNodeChangeListener implements IZkChildListener {

    @Override
    public void handleChildChange(String path, List<String> childrens) throws Exception {
        log.info("Child Change event at {}", path);
        log.info("childrens {}", childrens);
        ClusterInfo.getClusterInfo().getAllNodes().clear();
        ClusterInfo.getClusterInfo().getAllNodes().addAll(childrens);
    }
}

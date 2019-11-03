package com.vaibhav.dd.storage;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class ClusterInfo {

    private List<String> allNodes;
    private List<String> liveNodes;
    private String masterNode = "";

    private static class Singleton {

        private static final ClusterInfo instance = new ClusterInfo();
    }

    private ClusterInfo() {
        this.allNodes = new ArrayList<>();
        this.liveNodes = new ArrayList<>();
    }

    public static ClusterInfo getClusterInfo() {
        return Singleton.instance;
    }

}

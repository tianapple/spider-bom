package org.spider.es;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tianxudong on 2017/10/27.
 */
public class ElasticSearchClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchClient.class);

//    es.cluster.name=BJCluster
//    es.cluster.ipAddress=119.57.155.61
//    es.cluster.port=9300
//    es.index.name=bc_vrm
//    es.index.type=album
//    es.index.name.v3=bc_res
//    es.index.type.v3=movie

    private String clusterName;
    private String clusterIpAddress;
    private int clusterPort;
    private Map<String, Client> clientMap = new ConcurrentHashMap<>();

    public void init() {
        try {
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            List<InetSocketTransportAddress> addressList = initAddress(clusterIpAddress);
            addClient(settings, addressList);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 添加es客户端
     *
     * @param setting          配置
     * @param transportAddress 地址端口列表
     * @throws Exception
     */
    private void addClient(Settings setting, List<InetSocketTransportAddress> transportAddress) throws Exception {
        TransportClient client = TransportClient.builder().settings(setting).build();
        for (int i = 0; i < transportAddress.size(); i++) {
            client.addTransportAddress(transportAddress.get(i));
        }
        clientMap.put(setting.get("cluster.name"), client);
    }

    /**
     * 得所有的地址端口
     *
     * @param ips 通过,隔开的ip地址
     * @return 地址+端口列表
     * @throws Exception
     */
    private List<InetSocketTransportAddress> initAddress(String ips) throws Exception {
        List<InetSocketTransportAddress> addressList = new ArrayList<>();
        if (StringUtils.isNotBlank(ips) && ips.contains(",")) {
            String[] ipAtt = ips.split(",");
            for (String ip : ipAtt) {
                addressList.add(new InetSocketTransportAddress(InetAddress.getByName(ip), clusterPort));
            }
        } else {
            addressList.add(new InetSocketTransportAddress(InetAddress.getByName(ips), clusterPort));
        }
        return addressList;
    }

    public Client getClient() {
        return getClient(clusterName);
    }

    public Client getClient(String clusterName) {
        return clientMap.get(clusterName);
    }
}

package com.telephone.config;

import java.net.InetAddress;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.elasticsearch.common.settings.Settings;

@Configuration
public class Config {
    private static final Logger logger = Logger.getLogger(Config.class.getName());

    @Value("${elasticsearch.cluster.name:elasticsearch}")
    private String clusterName;

    @Value("${elasticsearch.cluster.address:127.0.0.1}")
    private String clusterAddress;

    @Value("${elasticsearch.cluster.port:9300}")
    private int clusterPort;

    @Bean(destroyMethod = "close")
    public Client client() {
        TransportClient client = null;

        try {
            final Settings elasticsearchSettings = Settings.builder()
                    .put("cluster.name", clusterName)
                    .put("client.transport.sniff", false)
                    .put("client.transport.ignore_cluster_name", true)
                    .build();
            logger.info("host:" + clusterAddress + "port:" + clusterPort);
            client = new PreBuiltTransportClient(elasticsearchSettings).
                    addTransportAddress(new TransportAddress(InetAddress.getByName(clusterAddress), clusterPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return client;
    }
}
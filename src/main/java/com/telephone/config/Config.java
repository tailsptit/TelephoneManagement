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

    @Value("${elasticsearch.host:localhost}")
    public String host;
    @Value("${elasticsearch.port:9300}")
    public int port;

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
                    .put("client.transport.sniff", true)
                    .build();
            logger.info("host:" + host + "port:" + port);
            client = new PreBuiltTransportClient(elasticsearchSettings).
                    addTransportAddress(new TransportAddress(InetAddress.getByName(clusterAddress), clusterPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return client;
    }
}

//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestClientBuilder;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Created by suman.das on 6/19/19.
// */
//@Configuration
//public class Config {
//    @Value("${elasticsearch.host:localhost}")
//    public String host;
//    @Value("${elasticsearch.port:9300}")
//    public int port;
//    public String getHost() {
//        return host;
//    }
//    public int getPort() {
//        return port;
//    }
//
//    private int timeout = 60;
//    @Bean
//    public RestHighLevelClient client(){
//        System.out.println("host:"+ host+"port:"+port);
//        final CredentialsProvider credentialsProvider =new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("elastic", "Fractal123456"));
//        RestClientBuilder builder =RestClient.builder(new HttpHost(host, port, "http")).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
//        builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(timeout * 1000).setSocketTimeout(timeout * 1000)
//                .setConnectionRequestTimeout(0));
//
//        RestHighLevelClient client = new RestHighLevelClient(builder);
//        return client;
//    }
//}
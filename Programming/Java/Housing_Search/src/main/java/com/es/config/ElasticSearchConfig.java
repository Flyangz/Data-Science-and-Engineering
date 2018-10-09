package com.es.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by LiuYang on 2018/10/5 10:42 PM
 */
@Configuration
public class ElasticSearchConfig {

    @Bean
    public TransportClient esclient() throws UnknownHostException {

        final Settings settings = Settings.builder()
                .put("cluster.name", "housingsearch")
                .put("client.transport.sniff", true)
                .build();

        // 配置一个 master 节点
        final InetSocketTransportAddress master = new InetSocketTransportAddress(
                InetAddress.getByName("localhost"),
                9300
        );

        final TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(master);

        return client;
    }
}

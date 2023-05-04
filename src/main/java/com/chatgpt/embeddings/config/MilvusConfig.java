package com.chatgpt.embeddings.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zero
 * @date 2023/4/28
 */
@Configuration
public class MilvusConfig {
    @Value("${milvus.ip}")
    private String milvusIp;
    @Value("${milvus.port}")
    private int milvusPort;

    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvusIp)
                .withPort(milvusPort)
                .build();
        return new MilvusServiceClient(connectParam);
    }
}

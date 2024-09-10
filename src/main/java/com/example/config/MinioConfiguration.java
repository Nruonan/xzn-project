package com.example.config;

import io.minio.MinioClient;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Configuration
@Slf4j
public class MinioConfiguration {
    @Value("${spring.minio.endpoint}")
    String endpoint;
    @Value("${spring.minio.username}")
    String username;

    @Value("${spring.minio.password}")
    String password;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder().endpoint(endpoint)
            .credentials(username,password).build();
    }
}

package com.aiott.ottpoc.config;

import com.aiott.ottpoc.adapter.out.storage.LocalMediaStorageAdapter;
import com.aiott.ottpoc.adapter.out.storage.r2.R2MediaStorageAdapter;
import com.aiott.ottpoc.application.port.out.MediaStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class StorageConfig {

    // ── Local storage (default) ──────────────────────────────────────────────

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
    public MediaStoragePort localMediaStorageAdapter() {
        return new LocalMediaStorageAdapter();
    }

    // ── Cloudflare R2 storage ────────────────────────────────────────────────

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
    public S3Client r2S3Client(R2Properties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(
                        "https://" + props.getAccountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())))
                .region(Region.of("auto"))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
    public MediaStoragePort r2MediaStorageAdapter(
            S3Client r2S3Client,
            R2Properties props,
            @Value("${app.storage.temp-dir:./data/tmp}") String tempDir) {
        return new R2MediaStorageAdapter(r2S3Client, props, tempDir);
    }
}

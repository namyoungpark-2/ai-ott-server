package com.aiott.ottpoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.r2")
public class R2Properties {
    private String accountId;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucket;
    /** Public base URL, e.g. {@code https://pub-xxxxxxxx.r2.dev} or a custom domain. */
    private String publicUrl;
}

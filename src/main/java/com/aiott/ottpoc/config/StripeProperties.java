package com.aiott.ottpoc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
@Getter @Setter
public class StripeProperties {
    private String secretKey;
    private String webhookSecret;
    private String basicPriceId;
    private String premiumPriceId;
}

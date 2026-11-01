package com.example.japaneseapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(SecurityProperties security, AiProperties ai) {

    public record SecurityProperties(JwtProperties jwt) {
    }

    public record JwtProperties(
            String issuer,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds,
            String secret
    ) {
    }

    public record AiProperties(
            String ollamaBaseUrl,
            String model
    ) {
    }
}

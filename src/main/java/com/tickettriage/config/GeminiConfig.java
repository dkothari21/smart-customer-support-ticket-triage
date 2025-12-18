package com.tickettriage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class GeminiConfig {

    @Value("${spring.gemini.api-key}")
    private String apiKey;

    @Value("${spring.gemini.model:gemini-1.5-flash}")
    private String modelName;

    @Value("${spring.gemini.temperature:0.3}")
    private float temperature;

    @Value("${spring.gemini.max-tokens:1000}")
    private int maxTokens;

    @Bean
    public String geminiApiKey() {
        return apiKey;
    }

    @Bean
    public String geminiModel() {
        return modelName;
    }

    @Bean
    public float geminiTemperature() {
        return temperature;
    }

    @Bean
    public int geminiMaxTokens() {
        return maxTokens;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}

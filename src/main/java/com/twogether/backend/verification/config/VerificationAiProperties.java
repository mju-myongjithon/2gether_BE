package com.twogether.backend.verification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.ai.verification")
public class VerificationAiProperties {
    private String mode = "mock";
    private String apiKey = "";
    private String model = "gemini-3.5-flash";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(15);
    private long maxImageSize = 5 * 1024 * 1024;
    private List<String> allowedImageDomains = new ArrayList<>();

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public long getMaxImageSize() { return maxImageSize; }
    public void setMaxImageSize(long maxImageSize) { this.maxImageSize = maxImageSize; }
    public List<String> getAllowedImageDomains() { return allowedImageDomains; }
    public void setAllowedImageDomains(List<String> allowedImageDomains) { this.allowedImageDomains = allowedImageDomains; }
}

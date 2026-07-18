package com.twogether.backend.chat.recommendation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.ai.chat-recommendation")
public class ChatRecommendationAiProperties {
    private String topicMode = "mock";
    private String missionMode = "mock";
    private String apiKey = "";
    private String model = "gemini-3.5-flash";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(15);

    public String getTopicMode() { return topicMode; }
    public void setTopicMode(String topicMode) { this.topicMode = topicMode; }
    public String getMissionMode() { return missionMode; }
    public void setMissionMode(String missionMode) { this.missionMode = missionMode; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
}

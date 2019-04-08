package io.ngin.websocket.sample.client.java;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("websocket")
public class WebSocketClientProperties {
    private String baseUrl;
    private String path;
    private String key;
    private String secret;
    private List<String> channels;
    private List<String> marketIds;
    
    public List<String> getChannels() {
        return channels;
    }
    public void setChannels(List<String> channels) {
        this.channels = channels;
    }
    public List<String> getMarketIds() {
        return marketIds;
    }
    public void setMarketIds(List<String> marketIds) {
        this.marketIds = marketIds;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
}
